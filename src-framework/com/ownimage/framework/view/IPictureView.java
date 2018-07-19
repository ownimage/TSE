package com.ownimage.framework.view;

import java.util.function.Consumer;

import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.control.control.IGrafitti;
import com.ownimage.framework.view.javafx.GrafittiImp;

public interface IPictureView extends IView {

	public void redrawGrafitti(Consumer<IGrafittiImp> pGrafitti);

	public IGrafittiImp updateGrafitti();

}
