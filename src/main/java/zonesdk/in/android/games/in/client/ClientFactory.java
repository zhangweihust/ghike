package zonesdk.in.android.games.in.client;


import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import zonesdk.in.android.games.in.client.annotation.DefaultFormParam;
import zonesdk.in.android.games.in.client.annotation.DefaultQueryParam;
import zonesdk.in.android.games.in.client.annotation.FileUpload;
import zonesdk.in.android.games.in.client.annotation.FormParam;
import zonesdk.in.android.games.in.client.annotation.GET;
import zonesdk.in.android.games.in.client.annotation.POST;
import zonesdk.in.android.games.in.client.annotation.PathParam;
import zonesdk.in.android.games.in.client.annotation.QueryParam;
import zonesdk.in.android.games.in.client.annotation.Version;
import zonesdk.in.android.games.in.common.Constants;

public class ClientFactory {

	private RequestProvider mProvider;
	
	private static final String BASE_URL = Constants.URL_TOUCH;
	
	private static final int MAX_LENGTH = 10 * 1024 * 1024;

	private static ClientFactory instance;
	
	private ClientFactory(RequestProvider provider){
		mProvider = provider;
	}
	
	public RequestProvider getProvider(){
		return mProvider;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> type){
		return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{type}, new ClientHandler());
	}
	
	public static ClientFactory getInstance(){
		synchronized(ClientFactory.class){
			if(instance == null)
				instance = new ClientFactory(RequestProvider.DEFAULT_PROVIDER);
		}
		return instance;
	}
	
	public static ClientFactory createInstance(RequestProvider provider){
		return new ClientFactory(provider);
	}
	
	private class ClientHandler implements InvocationHandler {
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			FileUpload upload = method.getAnnotation(FileUpload.class);
			GET get = method.getAnnotation(GET.class);
			POST post = method.getAnnotation(POST.class);
			Version version = method.getAnnotation(Version.class);
			APIVersion apiVersion = version == null?APIVersion.DEFAULT:version.value();
			if(upload != null){
				String url = parseUrl(upload.value(), method, args, apiVersion);
				MediaInputStream inStream = parseInputStream(method, args);
				if(inStream == null){
					throw new RuntimeException("Cannot find MediaInputStream as parameter from method " + method.getName() + ".");
				}
				return sendFile(url, inStream.getName(), inStream.getMimeType(), inStream, inStream.getContentLength(), method.getReturnType());
			}
			if(get != null){
				String url = parseUrl(get.value(), method, args, apiVersion);
				return sendGetRequest(url, method.getReturnType());
			}else if(post != null){
				String url = parseUrl(post.value(), method, args, apiVersion);
				Map<String, String> params = parseParams(method, args);
				return sendPostRequest(url, params, method.getReturnType());
			}else{
				throw new RuntimeException("Cannot invoke method without GET or POST annotation.");
			}
		}
		
		private String parseUrl(String tempUrl, Method method, Object[] args, APIVersion version){
			if(tempUrl.indexOf('?') == -1){
				tempUrl = tempUrl + "?platform=android";
			}else{
				tempUrl = tempUrl + "&platform=android";
			}
			DefaultQueryParam defaultQueryParam = method.getAnnotation(DefaultQueryParam.class);
			if(defaultQueryParam != null){
				String[] values = defaultQueryParam.value();
				for(String value:values){
					String[] pair = value.split("=");
					tempUrl = tempUrl + "&" + pair[0] + "=" + (pair.length>1?pair[1]:"");
				}
			}
			Annotation[][] annotationSets = method.getParameterAnnotations();
			for(int i=0;i<args.length;i++){
				Object value = args[i];
				if(value == null)
					continue;
				Annotation[] annotations = annotationSets[i];
				String queryParam = queryParam(annotations);
				String pathParam = pathParam(annotations);
				if(pathParam != null)
					tempUrl = tempUrl.replaceAll("\\{" + pathParam + "\\}", value.toString());
				
				if(queryParam != null){
					tempUrl = tempUrl + "&" + queryParam + "=" + value.toString();
				}
			}
			return version + tempUrl;
		}
		
		private String queryParam(Annotation[] annotations){
			for(Annotation annotation : annotations){
				if(QueryParam.class.isInstance(annotation)){
					return ((QueryParam)annotation).value();
				}
			}
			return null;
		}
		
		private String pathParam(Annotation[] annotations){
			for(Annotation annotation : annotations){
				if(PathParam.class.isInstance(annotation)){
					return ((PathParam)annotation).value();
				}
			}
			return null;
		}
		
		private String formParam(Annotation[] annotations){
			for(Annotation annotation : annotations){
				if(FormParam.class.isInstance(annotation)){
					return ((FormParam)annotation).value();
				}
			}
			return null;
		}
		
		private Map<String, String> parseParams(Method method, Object[] args){
			Map<String, String> params = new HashMap<String, String>();
			DefaultFormParam defaultFormParam = method.getAnnotation(DefaultFormParam.class);
			if(defaultFormParam != null){
				String[] values = defaultFormParam.value();
				for(String value:values){
					String[] pair = value.split("=");
					params.put(pair[0], pair.length>1?pair[1]:"");
				}
			}
			Annotation[][] annotationSets = method.getParameterAnnotations();
			for(int i=0;i<args.length;i++){
				Object value = args[i];
				if(value == null)
					continue;
				Annotation[] annotations = annotationSets[i];
				String formParam = formParam(annotations);
				if(formParam == null)
					continue;
				params.put(formParam, value.toString());
			}
			return params;
		}
		
		private MediaInputStream parseInputStream(Method method, Object[] args){
			if(args[0] != null && MediaInputStream.class.isInstance(args[0]))
				return (MediaInputStream)args[0];
			return null;
		}
		
		private Object sendGetRequest(String url, Class<?> returnType) throws Exception{
			String result = mProvider.get(BASE_URL + url);
			return handleResult(result, returnType);
		}
		
		private Object sendPostRequest(String url, Map<String, String> params, Class<?> returnType) throws Exception{
			String result = mProvider.post(BASE_URL + url, params);
			return handleResult(result, returnType);
		}
		
		private Object sendFile(String urlStr, String fileName, String mimeType, InputStream inStream, int contentLength, Class<?> returnType) throws Exception{
			if(contentLength > MAX_LENGTH)
				throw new IOException("The file size exceeds the limit.");
			String result = mProvider.upload(BASE_URL + urlStr, fileName, mimeType, inStream, contentLength);
			return handleResult(result, returnType);
		}
		
		private Object handleResult(String result, Class<?> returnType) throws Exception{
			if(returnType.isAssignableFrom(JSONObject.class)){
				return new JSONObject(result);
			}else if(returnType.isAssignableFrom(String.class)){
				return result;
			}
			return null;
		}
		
	}
	
}
