package com.jenetics.smocker.rest;

import java.util.List;

import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.jenetics.smocker.jseval.SmockerJsEnv;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.model.config.SmockerConf;
import com.jenetics.smocker.rest.model.HostAndPortRangeModel;
import com.jenetics.smocker.rest.model.ListDuplicatedHosts;
import com.jenetics.smocker.rest.model.ListHostAndRangeModel;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.util.DuplicateHost;
import com.jenetics.smocker.ui.util.HostAndPortRange;
import com.jenetics.smocker.ui.view.ConfigView;
import com.jenetics.smocker.ui.view.MockSpaceView;
import com.vaadin.ui.Component;

@Stateless
@Path("/configurations")
@Produces("application/json")
@Consumes("application/json")
public class SmockerConfEndpoint extends AbstractEndpoint<SmockerConf> {
	
	@POST
	@Path("/clearEnv")
	public Response clear() {
		SmockerJsEnv.getInstance().clear();
		return Response.ok().build();
	}
	
	@GET
	@Path("/dupHosts")
	public Response dupHosts() {
		ConfigView configView = getConfigView();
		List<DuplicateHost> duplicateHosts = configView.getDuplicateHosts();
		ListDuplicatedHosts listDuplicatedHosts = new ListDuplicatedHosts();
		for (DuplicateHost duplicateHost : duplicateHosts) {
			listDuplicatedHosts.addDuplicatedHost(duplicateHost);
		}
		return Response.ok().entity(listDuplicatedHosts).build();
	}
	
	@GET
	@Path("/excludedHosts")
	public Response ignoredHosts() {
		ConfigView configView = getConfigView();
		List<HostAndPortRange> excludedHosts = configView.getExcludedHosts();
		ListHostAndRangeModel listExcludedHosts = new ListHostAndRangeModel();
		if (excludedHosts != null) {
			fillHostAndPortRange(excludedHosts, listExcludedHosts);
		}
		return Response.ok().entity(listExcludedHosts).build();
	}
	
	@GET
	@Path("/includedHosts")
	public Response incincludedHostsuded() {
		ConfigView configView = getConfigView();
		List<HostAndPortRange> includedHosts = configView.getIncludedHosts();
		ListHostAndRangeModel listIncludedHosts = new ListHostAndRangeModel();
		if (includedHosts != null) {
			fillHostAndPortRange(includedHosts, listIncludedHosts);
		}
		return Response.ok().entity(listIncludedHosts).build();
	}

	private void fillHostAndPortRange(List<HostAndPortRange> hostAndPortRangeList, 
			ListHostAndRangeModel listHostAndRangeModel) {
		for (HostAndPortRange hostAndPortRange : hostAndPortRangeList) {
			HostAndPortRangeModel hostAndPortRangeModel = 
					new HostAndPortRangeModel(hostAndPortRange.getHost());
			int[] portRangeArray = HostAndPortRange.getPortRange(hostAndPortRange.getPortRange());
			if (portRangeArray != null) {
				if (portRangeArray.length == 1) {
					hostAndPortRangeModel.setMinPort(portRangeArray[0]);
				}
				else if (portRangeArray.length == 2) {
					hostAndPortRangeModel.setMinPort(portRangeArray[0]);
					hostAndPortRangeModel.setMaxPort(portRangeArray[1]);
				}
				listHostAndRangeModel.addIHostAndPortRangeModel(hostAndPortRangeModel);
			}
		}
	}

	private ConfigView getConfigView() {
		return (ConfigView) SmockerUI.getInstance().getEasyAppMainView().getScanner()
				.getViewMap().get(ConfigView.class.toString());
	}
	
}	
