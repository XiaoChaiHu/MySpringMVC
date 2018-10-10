package com.wb.myspringmvc.servlet;

import com.wb.myspringmvc.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "dispatcherServlet", urlPatterns = "/*", loadOnStartup = 1, initParams = {
        @WebInitParam(name = "base-package", value = "com.wb.myspringmvc")
})
public class DispatcherServlet extends HttpServlet {

    /**
     * 基础扫描包
     */
    private String basePackage = "";

    private List<String> packageName = new ArrayList<>();

    private Map<String, Object> instanceMap = new HashMap<>();

    private Map<String, String> nameMap = new HashMap<>();

    private Map<String, Method> urlMethodMap = new HashMap<>();

    private Map<Method, String> methodPackageMap = new HashMap<>();

    /**
     * 初始化
     *
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        basePackage = config.getInitParameter("base-package");

        try {
            //扫描
            this.scanBasePackage(basePackage);
            //实例化
            this.instance(packageName);
            this.SpringIoc();
            this.handlerUrlMethodMap();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * 扫描基包
     *
     * @param basePackage
     */
    private void scanBasePackage(String basePackage) {
        URL url = this.getClass().getClassLoader().getResource(basePackage.replaceAll("\\.", "/"));
        File basePackageFile = new File(url.getPath());
        System.out.println("scan:" + basePackageFile);
        File[] childFiles = basePackageFile.listFiles();
        for (File file : childFiles) {
            if (file.isDirectory()) {//目录继续递归扫描
                this.scanBasePackage(basePackage + "." + file.getName());
            } else if (file.isFile()) {
                packageName.add(basePackage + "." + file.getName().split("\\.")[0]);
            }
        }
    }

    private void instance(List<String> packageName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (packageName.size() < 1) {
            return;
        }
        for (String string : packageName) {
            Class<?> c = Class.forName(string);
            if (c.isAnnotationPresent(Controller.class)) {
                Controller controller = c.getAnnotation(Controller.class);
                String controllerName = controller.value();
                instanceMap.put(controllerName, c.newInstance());
                nameMap.put(string, controllerName);
            } else if (c.isAnnotationPresent(Service.class)) {
                Service service = c.getAnnotation(Service.class);
                String serviceName = service.value();
                instanceMap.put(serviceName, c.newInstance());
                nameMap.put(string, serviceName);
            } else if (c.isAnnotationPresent(Repository.class)) {
                Repository repository = c.getAnnotation(Repository.class);
                String repositoryName = repository.value();
                instanceMap.put(repositoryName, c.newInstance());
                nameMap.put(string, repositoryName);
            }
        }
    }

    private void SpringIoc() throws IllegalAccessException {
        for (Map.Entry<String, Object> entry : instanceMap.entrySet()) {
            Field[] declaredFields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Qualifier.class)) {
                    String name = field.getAnnotation(Qualifier.class).value();
                    field.setAccessible(true);
                    field.set(entry.getValue(), instanceMap.get(name));
                }
            }
        }
    }

    private void handlerUrlMethodMap() throws ClassNotFoundException {
        if (packageName.size() < 1) {
            return;
        }
        for (String string : packageName) {
            Class<?> c = Class.forName(string);
            if (c.isAnnotationPresent(Controller.class)) {
                Method[] methods = c.getMethods();
                StringBuffer baseUrl = new StringBuffer();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                        baseUrl.append(annotation.value());
                        urlMethodMap.put(baseUrl.toString(), method);
                        methodPackageMap.put(method, string);
                    }
                }
            }

        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getRequestURL().toString();
        String contextPath = req.getContextPath();
        String path=url.replace(contextPath,"");
        //通过path找到Method
        Method method = urlMethodMap.get(path);
        if (method!=null){
            //通过method获取controller对象进行反射
            String packageName = methodPackageMap.get(method);
            String controllerName = nameMap.get(packageName);
            //拿到controller对象
            Object controller = instanceMap.get(controllerName);
            method.setAccessible(true);
            try {
                method.invoke(controller);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
