package com.jenetics.smocker.ui.view;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.jenetics.smocker.annotation.ContentView;
import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

@SpringComponent
@UIScope
@ContentView (viewName = "Connections", icon = "icons/Places-network-server-database-icon.png")
public class ConnectionsView extends VerticalLayout implements RefreshableView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPAContainer<Connection> connections = null;
	private Table connectionTable;
	
	
	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub
		
		connections = JPAContainerFactory.make(Connection.class,
	            SmockerUI.PERSISTENCE_UNIT);
		connectionTable = new Table(null, connections);
        connectionTable.setSelectable(true);
        connectionTable.setSizeFull();
        setSizeFull();
        addComponent(connectionTable);
	}
	
	@Override
	public void refresh() {
		connections.refresh();
	}
}
