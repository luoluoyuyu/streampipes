package de.fzi.cep.sepa.manager.appstore;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.fzi.cep.sepa.appstore.shared.BundleInfo;
import de.fzi.cep.sepa.appstore.shared.InstallationStatus;
import de.fzi.cep.sepa.appstore.shared.UninstallStatus;
import de.fzi.cep.sepa.commons.config.ConfigurationManager;
import de.fzi.cep.sepa.messages.AppInstallationMessage;
import de.fzi.cep.sepa.messages.Message;
import de.fzi.cep.sepa.messages.Notifications;
import de.fzi.cep.sepa.storage.api.StorageRequests;
import de.fzi.cep.sepa.storage.controller.StorageManager;
import de.fzi.cep.sepa.storage.service.UserService;


public class AppStoreInfoProvider {

	private final static String APPSTORE_URL = "/appstore";
	private final static String POD_URL = "/deploy";
	
	public List<BundleInfo> getAvailableApps() {
		try {
			String bundleInfo = Request
				.Get(getAppStoreUrl())
				.execute()
				.returnContent()
				.asString();
			List<BundleInfo> storeBundles = new Gson().fromJson(bundleInfo, new TypeToken<List<BundleInfo>>(){}.getType());
			new InstalledAppsInfoProvider().updateAppStoreInfoWithInstalledApps(storeBundles);
			return storeBundles;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	private String getAppStoreUrl() {
		return ConfigurationManager.getWebappConfigurationFromProperties().getMarketplaceUrl() +APPSTORE_URL;
	}

	public AppInstallationMessage installApplication(String username, BundleInfo bundle) {
		Gson gson = new Gson();
		bundle.setAppStoreUrl(getAppStoreUrl());
		
		try {
			String response = Request.Post(bundle.getTargetPodUrl() +POD_URL)
				.bodyString(gson.toJson(bundle), ContentType.APPLICATION_JSON)
				.execute()
				.returnContent()
				.asString();
			InstallationStatus installationStatus = gson.fromJson(response, InstallationStatus.class);

			if (installationStatus.isSuccess()) {
				List<Message> importMessages = new GraphInstaller(extractUrls(bundle), username).install();
				bundle.setInstalled(true);
				bundle.setInstallationTimestamp(System.currentTimeMillis());
				StorageManager.INSTANCE.getAppStorageApi().storeBundle(bundle);
				return new AppInstallationMessage(true, importMessages);
			}
			else return new AppInstallationMessage(false, installationStatus.getErrorMessage());
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return new AppInstallationMessage(false, "Could not reach stream processing pod.");
		} catch (IOException e) {
			e.printStackTrace();
			return new AppInstallationMessage(false, "Could not reach stream processing pod.");
		}
				
	}
	
	public Message uninstallApplication(String username, BundleInfo bundle) {
		Gson gson = new Gson();
		BundleInfo storedBundle = StorageManager
				.INSTANCE
				.getAppStorageApi()
				.getInstalledBundles()
				.stream()
				.filter(b -> b.getBundleId() == bundle.getBundleId())
				.findFirst()
				.get();
		
		List<String> elementUris = extractUrls(storedBundle);
		// TODO: check if any pipelines are running 
		for(String elementUri : elementUris) {
			try {
				delete(username, elementUri);
				StorageManager.INSTANCE.getAppStorageApi().deleteBundle(storedBundle);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		
		try {
			String response = Request
					.Delete(bundle.getTargetPodUrl())
					.bodyString(gson.toJson(bundle), ContentType.APPLICATION_JSON)
					.execute()
					.returnContent()
					.asString();
			
			UninstallStatus status = gson.fromJson(response, UninstallStatus.class);
			if (status.isSuccess()) return Notifications.success("Application uninstalled.");
			else return Notifications.error("Error occurred during uninstall.");
		} catch (Exception e) {
			return Notifications.error("An error occurred during uninstall.");
		}
		
	}
	
	private List<String> extractUrls(BundleInfo bundle) {
		String response;
		try {
			response = Request.Get(makeLocalInstalledBundleUrl(bundle)).addHeader("Accept", "application/json").execute().returnContent().asString();
			return new Gson().fromJson(response, new TypeToken<List<String>>(){}.getType());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return new ArrayList<>();
		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
	private String makeLocalInstalledBundleUrl(BundleInfo bundle) {
		return bundle.getTargetPodUrl() +"/" +bundle.getAppContextPath();
	}
	
	private void delete(String username, String elementId) throws URISyntaxException {
		StorageRequests requestor = StorageManager.INSTANCE.getStorageAPI();
		UserService userService = StorageManager.INSTANCE.getUserService();
		if (requestor.getSEPAById(elementId) != null) 
		{
			requestor.deleteSEPA(requestor.getSEPAById(elementId));
			userService.deleteOwnSepa(username, elementId);
		}
		else if (requestor.getSEPById(elementId) != null) 
		{
			requestor.deleteSEP(requestor.getSEPById(elementId));
			userService.deleteOwnSource(username, elementId);
		}
		else if (requestor.getSECById(elementId) != null) 
		{
			requestor.deleteSEC(requestor.getSECById(elementId));
			userService.deleteOwnAction(username, elementId);
		}
	}
}
