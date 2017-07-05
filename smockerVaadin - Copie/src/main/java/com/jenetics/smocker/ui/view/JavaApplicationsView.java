package com.jenetics.smocker.ui.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.vaadin.easyapp.util.annotations.ContentView;

import com.jenetics.smocker.model.Connection;
import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.model.JavaApplication;
import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.util.EventManager;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.annotations.Push;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.Position;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Push
@ViewScope
@ContentView(sortingOrder=1, viewName = "Java Applications", icon = "icons/Java-icon.png", homeView=true, rootViewParent=ConnectionsRoot.class)
public class JavaApplicationsView extends VerticalLayout implements RefreshableView {


	@Inject
	private Logger logger;

	protected static final String NAME_PROPERTY = "Name";
	protected static final String HOURS_PROPERTY = "Hours done";
	protected static final String MODIFIED_PROPERTY = "Last Modified";


	private TreeTable treetable= null;
	private JPAContainer<JavaApplication> jpaJavaApplication;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JavaApplicationsView() {
		jpaJavaApplication = JPAContainerFactory.make(JavaApplication.class, SmockerUI.PERSISTENCE_UNIT);
		setMargin(true);
		treetable = new TreeTable();
		treetable.setSelectable(true);
		treetable.addContainerProperty("Application", String.class, "");
		treetable.addContainerProperty("Adress", String.class, "");
		treetable.addContainerProperty("Port", String.class, "");
		treetable.addContainerProperty("ConnectionType", String.class, "");
		treetable.setSizeFull();
		fillTreeTable();
		addComponent(treetable);
		setSizeFull();
	}

	private Object rootTreeItem = null;

	private void fillTreeTable() {
		Collection<Object> itemIds = jpaJavaApplication.getItemIds();

		Object[] root = new Object[] { "all", "", "", "" };
		rootTreeItem = treetable.addItem(root, null);
		for (Object id : itemIds) {
			JavaApplication javaApplication = jpaJavaApplication.getItem(id).getEntity();
			Object javaApplicationTreeItem = buildJavaApplicationTreeItem(javaApplication);
			Set<Connection> connections = javaApplication.getConnections();

			for (Connection connection : connections) {
				buildConnectionTreeItem(connection);
			}
			if (connections.size() == 0) {
				treetable.setChildrenAllowed(javaApplicationTreeItem, false);
			}
		}
	}

	private Map<Object ,Object > applicationItemById = new HashMap<>();

	/**
	 * Update the tree add new items (JavaConnection or Connection) 
	 * @param entityWithId
	 */
	private void updateTree(EntityWithId entityWithId) {
		if (entityWithId instanceof JavaApplication) {
			JavaApplication javaApplication = (JavaApplication) entityWithId;
			buildJavaApplicationTreeItem(javaApplication);
		}
		else if (entityWithId instanceof Connection)
		{
			Connection conn = (Connection) entityWithId;
			buildConnectionTreeItem(conn);

		}
	}


	private void buildConnectionTreeItem(Connection conn) {

		Object javaApplicationTreeItem = applicationItemById.get(conn.getJavaApplication().getId());
		if (javaApplicationTreeItem != null) {
			Object[] itemConnection = new Object[] { conn.getJavaApplication().getClassQualifiedName(),  conn.getHost(), conn.getPort().toString(), ""};
			Object connectionTreeItem = treetable.addItem(itemConnection, null);
			treetable.setChildrenAllowed(javaApplicationTreeItem, true);
			treetable.setParent(connectionTreeItem, javaApplicationTreeItem);
			treetable.setChildrenAllowed(connectionTreeItem, false);
		}
		else {
			logger.warn("Unable to find javaApplicationTreeItem");
		}
	}


	private Object buildJavaApplicationTreeItem(JavaApplication javaApplication) {
		Object[] javaApplicationItem = new Object[] { javaApplication.getClassQualifiedName(),  "", "", "" };
		Object javaApplicationTreeItem = treetable.addItem(javaApplicationItem, null);
		treetable.setParent(javaApplicationTreeItem, rootTreeItem);
		treetable.setChildrenAllowed(javaApplicationTreeItem, false);
		applicationItemById.put(javaApplication.getId(), javaApplicationTreeItem);
		return javaApplicationTreeItem;
	}


	@Override
	public void enter(ViewChangeEvent event) {

	}

	@Override
	public void refresh(EntityWithId entityWithId) {
		Notification notif = new Notification(
				"Warning",
				"Area of reindeer husbandry",
				Notification.TYPE_WARNING_MESSAGE);

		// Customize it
		notif.setDelayMsec(100);
		notif.setPosition(Position.BOTTOM_RIGHT);
		notif.setIcon(FontAwesome.SPINNER);

		// Show it in the page
		notif.show(Page.getCurrent());
		
		treetable.setEnabled(true);
		
		jpaJavaApplication.refreshItem(entityWithId.getId());
	}
}