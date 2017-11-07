package com.ownimage.perception.view.fx;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.layout.ContainerList;
import com.ownimage.framework.control.layout.HFlowLayout;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.javafx.AppControlView;
import com.ownimage.framework.view.javafx.FXViewFactory;

import javafx.application.Application;

public class AccordionTest extends AppControlView {

	class TestTransform extends Container {

		public TestTransform(final String pDisplayName, final String pPropertyName, final int pNumberOfControls) {
			super(pDisplayName, pPropertyName);

			for (int i = 0; i < pNumberOfControls; i++) {
				IntegerControl ic = new IntegerControl(pDisplayName + " " + i, pPropertyName + i, this, i);
			}
		}

	}

	public AccordionTest(final String pTitle) {
		super();
	}

	public static void main(final String[] pArgs) {
		FXViewFactory.setAsViewFactory();
		AccordionTest appControl = new AccordionTest("Accordion Test");
		Application app = (AppControlView) appControl.createContentView();
		app.launch(app.getClass());
	}

	protected IView createContentView() {

		ContainerList transformSequence = new ContainerList("Container List", "containerList");

		IContainer t1 = new TestTransform("Transform 1", "transform1", 3);
		IContainer t2 = new TestTransform("Transform 2", "transform2", 4);

		transformSequence.add(t1);
		transformSequence.add(t2);

		HFlowLayout hflow = new HFlowLayout(transformSequence, transformSequence);
		return hflow.createView();
	}
}
