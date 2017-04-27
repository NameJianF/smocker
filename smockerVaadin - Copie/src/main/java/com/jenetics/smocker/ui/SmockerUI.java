package com.jenetics.smocker.ui;

import java.util.Collections;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.logging.Logger;
import org.vaadin.easyapp.EasyAppBuilder;
import org.vaadin.easyapp.EasyAppMainView;
import org.vaadin.easyapp.util.MessageBuilder;

import com.jenetics.smocker.model.EntityWithId;
import com.jenetics.smocker.ui.util.RefreshableView;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Image;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@Push
@Theme("smocker")
public class SmockerUI extends UI {

	private static final int SLEEP_TIME = 100;

	@PersistenceContext(unitName = SmockerUI.PERSISTENCE_UNIT)
	private EntityManager em;

	@Inject
	private Logger logger;

	public static final String PERSISTENCE_UNIT = "smockerLocalData";

	Navigator navigator;
	protected static final String MAINVIEW = "main";

	private static SmockerUI instance = null;

	public EntityManager getEm() {
		return em;
	}


	public static SmockerUI getInstance() {
		return instance;
	}


	private EasyAppMainView easyAppMainView; 


	@Override
	protected void init(VaadinRequest request) {
		
		final VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        
        
        Image image = new Image(null, new ThemeResource("favicon.ico"));
		image.setWidth(50, Unit.PIXELS);
		image.setHeight(50, Unit.PIXELS);
		
		easyAppMainView = new EasyAppBuilder(Collections.singletonList("com.jenetics.smocker.ui.view"))
        	.withTopBarIcon(image)
        	.withTopBarStyle("topBannerBackGround")
        	.withSearchCapabilities( (searchValue) -> search(searchValue) , FontAwesome.SEARCH)
        	.withBreadcrumb()
        	.withBreadcrumbStyle("breadcrumbStyle")
        	.withButtonLinkStyleInBreadCrumb(BaseTheme.BUTTON_LINK)
        	//.withLoginPopupLoginStyle("propupStyle")
        	.build();
	
		
		layout.addComponents(easyAppMainView);
        
		easyAppMainView.getTopBar().setStyleName("topBannerBackGround");
        
        setContent(layout);
	}


	private Object search(String searchValue) {
		// TODO Auto-generated method stub
		return null;
	}


	public void refreshView(String viewName, EntityWithId entityWithId) {
		access(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(SLEEP_TIME);
					((RefreshableView)easyAppMainView.getNavigator().getCurrentView()).refresh(entityWithId);
				} catch (InterruptedException e) {
					logger.error(MessageBuilder.getEasyAppMessage("Unable to get the view map"), e);
				}
			}
		});
	}

}
