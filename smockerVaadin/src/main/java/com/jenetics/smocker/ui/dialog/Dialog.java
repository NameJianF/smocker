package com.jenetics.smocker.ui.dialog;

import java.util.List;
import java.util.function.Consumer;

import org.jboss.logging.Logger;

import com.jenetics.smocker.ui.SmockerUI;
import com.jenetics.smocker.ui.component.BoxableItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

import de.steinwedel.messagebox.ButtonType;
import de.steinwedel.messagebox.MessageBox;

public class Dialog {

	private static Logger logger = Logger.getLogger(Dialog.class);
	
	private Dialog() {
		super();
	}


	public static void ask(String question, String caption, DialogProcess yesProcess, DialogProcess noProcess) {
		MessageBox.createQuestion().withCaption(caption).withMessage(question).withYesButton(() -> {
			if (yesProcess != null) {
				try {
					yesProcess.process();
				} catch (Exception e) {
					logger.error("Unable to process yes", e);
				}
			}
		}).withNoButton(() -> {
			if (noProcess != null) {
				try {
					noProcess.process();
				} catch (Exception e) {
					logger.error("Unable to process no", e);
				}
			}
		}).open();
	}
	
	public static void displaySelectableListBox(String caption, List<String> options, Consumer<String> selected) {
		ComboBox<String> comboBox = new ComboBox<>();
		comboBox.setEmptySelectionAllowed(false);
		comboBox.setItems(options);
		
		MessageBox box = MessageBox.create();
		box.withCaption(caption).withMessage(comboBox).withCancelButton();
		box.withOkButton(() -> {
			selected.accept(comboBox.getSelectedItem().get());
		});
		
		box.open();
	}
	
	public static <T> MessageBox displayComponentBox(String caption, Consumer<T> selected, 
			BoxableItem<T> boxableItem) {
		MessageBox box = MessageBox.create();
		box.withCaption(caption).withMessage(boxableItem.getComponent()).withCancelButton();
		box.withOkButton(() -> {
			selected.accept(boxableItem.getItem());
		});
		box.open();
		return box;
	}

	public static void displayCreateStringBox(String caption, Consumer<String> selected) {
		TextField textField = new TextField();
		
		MessageBox box = MessageBox.create();
		box.withCaption(caption).withMessage(textField).withCancelButton();
		box.withOkButton(() -> {
			selected.accept(textField.getValue());
		});
		
		box.open();
		
	}
	
	public static void displayCreateStringBox(String caption, Consumer<String> selected, String initialValue) {
		TextField textField = new TextField();
		if (initialValue == null) {
			initialValue = "";
		}
		textField.setValue(initialValue);
		MessageBox box = MessageBox.create();
		box.withCaption(caption).withMessage(textField).withCancelButton();
		box.withOkButton(() -> {
			selected.accept(textField.getValue());
		});
		
		box.open();
		
	}


	public static void warning(String value) {
		MessageBox box = MessageBox.createInfo().withMessage(value);
		box.open();
		
	}
	

}
