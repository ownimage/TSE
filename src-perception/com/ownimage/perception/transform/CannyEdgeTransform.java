/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2014 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform;

import static com.ownimage.framework.control.container.NullContainer.NullContainer;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.ColorControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.ObjectControl;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.SplitTimer;
import com.ownimage.framework.util.Version;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.math.Rectangle;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.render.ITransformResult;
import com.ownimage.perception.transform.cannyEdge.CannyEdgeDetectorFactory;
import com.ownimage.perception.transform.cannyEdge.EditPixelMapDialog;
import com.ownimage.perception.transform.cannyEdge.GenerateEdgesDialog;
import com.ownimage.perception.transform.cannyEdge.ICannyEdgeDetector;

public class CannyEdgeTransform extends BaseTransform implements IPixelMapTransformSource {

    public enum LineEndLengthType {
        Percent, Pixels
    }

    public enum LineEndShape {
        Square, Straight, Curved
    }

    public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
    public final static String mClassname = CannyEdgeTransform.class.getName();

    public final static Logger mLogger = Logger.getLogger(mClassname);
    public final static long serialVersionUID = 1L;

    private final IntegerControl mWidth =
            new IntegerControl("Width", "width", getContainer(), 1000, 100, 50000, 500).setEnabled(false);
    private final IntegerControl mHeight =
            new IntegerControl("Height", "height", getContainer(), 1000, 100, 50000, 500).setEnabled(false);

    private final ActionControl mGeneratePixelMapButton =
            new ActionControl("Generate Edges", "generate", getContainer(), this::generateEdges);
    private final ActionControl mEditPixelMapButton =
            new ActionControl("Edit Pixels", "editPixels", getContainer(), this::editPixels)
                    .setEnabled(false);

    // TODO private final EditPixelMapDialog mEditPixelMapSegmentsDialog;
    // TODO private final EditPixelMapDialog mEditPixelMapPixelsDialog;

    private GenerateEdgesDialog mGenerateEdgesDialog;
    private EditPixelMapDialog mEditPixelMapDialog;

    private final DoubleControl mWhiteFade =
            new DoubleControl("White Fade", "whiteFade", getContainer(), 0.0d);

    private final BooleanControl mShowPixels =
            new BooleanControl("Show Pixels", "showPixels", getContainer(), false);

    private final ColorControl mPixelColor =
            new ColorControl("Pixel Colour", "pixelColor", getContainer(), Color.BLACK);
    private final DoubleControl mLineTolerance =
            new DoubleControl("Line Tolerance", "lineTolerance", getContainer(), 1.2d, 0.1d, 10.0d);
    private final DoubleControl mLineCurvePreference =
            new DoubleControl("Curve Preference", "curvePreference", getContainer(), 1.2d, 0.1d, 100.0d);

    private final BooleanControl mLinesShow =
            new BooleanControl("Show Lines", "showLines", getContainer(), false);
    private final ObjectControl<LineEndShape> mLineEndShape =
            new ObjectControl<>("Line End Shape", "lineEndShape", getContainer(), LineEndShape.Square, LineEndShape.values());
    private final ObjectControl<LineEndLengthType> mLineEndLengthType =
            new ObjectControl<>(
                    "Line End Length Type",
                    "lineEndLengthType",
                    getContainer(),
                    LineEndLengthType.Pixels,
                    LineEndLengthType.values());

    private final IntegerControl mLineEndLengthPercent =
            new IntegerControl("Length Percent", "lineEndLengthPercent", getContainer(), 10, 1, 50, 5);
    private final IntegerControl mLineEndLengthPixels =
            new IntegerControl("Length Pixels", "lineEndLengthPixels", getContainer(), 50, 1, 500, 10);
    private final DoubleControl mLineEndThickness =
            new DoubleControl("Line End Thickness", "LineEndThickness", getContainer(), 0.5d);
    private final ColorControl mLineColor =
            new ColorControl("Line Color", "lineColor", getContainer(), Color.BLACK);
    private final DoubleControl mLineOpacity =
            new DoubleControl("Line Opacity", "lineOpacity", getContainer(), 1.0d);

    private final IntegerControl mLongLineLength =
            new IntegerControl("Long Line Length", "longLineLength", getContainer(), 50, 1, 500, 10);
    private final DoubleControl mLongLineThickness =
            new DoubleControl("Long Line Thickness", "longLineThickness", getContainer(), 1.0d, 0.0d, 10.0d);
    private final IntegerControl mMediumLineLength =
            new IntegerControl("Medium Line Length", "mediumLineLength", getContainer(), 0, 0, 1000, 20);
    private final DoubleControl mMediumLineThickness =
            new DoubleControl("Medium Line Thickness", "mediumLineThickness", getContainer(), 1.0d, 0.0d, 10.0d);

    private final IntegerControl mShortLineLength =
            new IntegerControl("Short Line Length", "shortLineLength", getContainer(), 0, 0, 1000, 20);
    private final DoubleControl mShortLineThickness =
            new DoubleControl("Short Line Thickness", "shortLineThickness", getContainer(), 1.0d, 0.0d, 10.0d);
    // shadow
    private final BooleanControl mShowShadow =
            new BooleanControl("Show Shadow", "showShadow", getContainer(), false);
    private final DoubleControl mShadowXOffset =
            new DoubleControl("Shadow X Offset", "shadowXOffset", getContainer(), 1.0d, -20.0d, 20.0d);

    // TODO private final ObjectControl<String> mEqualize = new ObjectControl<>("Equalize Lengths", "equalize", getContainer(),
    // EqualizeValues.getDefaultValue(), EqualizeValues.getAllValues());

    private final DoubleControl mShadowYOffset =
            new DoubleControl("Shadow Y Offset", "shadowYOffset", getContainer(), 1.0d, -20.0d, 20.0d);
    private final DoubleControl mShadowThickness =
            new DoubleControl("Shadow Thickness", "shadowThickness", getContainer(), 1.0d, 1.0d, 10.0d);

    private final ColorControl mShadowColor =
            new ColorControl("Shadow Colour", "shadowColor", getContainer(), Color.WHITE);
    private final DoubleControl mShadowOpacity =
            new DoubleControl("Shadow Opacity", "shadowOpacity", getContainer(), 1.0d);

    private PixelMap mPixelMap; // this is the picture from the file processed for edges

    private PictureControl mPreviewPictureControl;

    public CannyEdgeTransform(final Perception pPerception) {
        super("Canny Edge", "cannyEdge");
        // getPreviewImage().setGrafitti(this);

        // mWidth.setEnabled(false);
        // mHeight.setEnabled(false);

        // TODO mEditPixelMapPixelsDialog = EditPixelMapDialog.createPixelEditorDialog(this, "Edit PixelMapPixels",
        // "editPixelMapPixels");
        // TODO mEditPixelMapSegmentsDialog = EditPixelMapDialog.createSegmentEditDialog(this, "Edit PixelMapSegments",
        // "editPixelMapSegments");

        // setUseTransform(false);
        // TODO makePersistant(mGenerateEdgesDialog);
        // TODO makePersistant(mEditPixelMapPixelsDialog);
        // TODO makePersistant(mEditPixelMapSegmentsDialog);

        // setPopupMenuControls(mUseTransform, mGeneratePixelMapButton, mEditPixelMapButton);

    }

    // @Override
    // public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
    // mLogger.fine("CannyEdgeTransform:controlChangeEvent " + pControl == null ? "null" : pControl.getDisplayName() + " " +
    // pIsMutating);
    //
    // if (!isMutating()) {
    // try {
    // if (!isInitialized()) { return; }
    //
    // if (pControl == mGeneratePixelMapButton) {
    // mGenerateEdgesDialog.showModalDialog();
    // return;
    // }
    //
    // if (pControl == mEditPixelMapButton) {
    // System.out.println("EditPixels");
    // mEditPixelMapPixelsDialog.showModalDialog();
    // return;
    // }
    //
    // if (mGenerateEdgesDialog.contains(pControl)) { return; }
    //
    // if (pControl == mShortLineLength || pControl == mMediumLineLength || pControl == mLongLineLength) {
    // try {
    // setMutating(true);
    //
    // getPixelMap().setPixelChainDefaultThickness(this);
    // mEqualize.setValue(EqualizeValues.getDefaultValue());
    // } finally {
    // setMutating(false);
    // }
    // }
    //
    // if (pControl.isOneOf(mLineTolerance, mLineCurvePreference) && !pIsMutating) {
    // reapproximate();
    // }
    //
    // if (pControl == mEqualize) {
    // try {
    // setMutating(true);
    //
    // System.out.println("Equalize");
    // if (mPixelMap != null) {
    // final EqualizeValues values = (EqualizeValues) mEqualize.getObjectValue();
    // mPixelMap.equalizeValues(values);
    // mShortLineLength.setValue(values.getShortLineLength());
    // mMediumLineLength.setValue(values.getMediumLineLength());
    // mLongLineLength.setValue(values.getLongLineLength());
    // getPixelMap().setPixelChainDefaultThickness(this);
    // }
    // } finally {
    // setMutating(false);
    // }
    // }
    // } finally {
    // CannyEdgeTransform.super.controlChangeEvent(pControl, pIsMutating);
    // }
    // }
    //
    // }
    //
    // @Override
    // public ITransformUI createUI() {
    // return GUIFactory.getInstance().createUI((IDynamicTransformUI) this);
    // }
    //
    private void editPixels() {
        ActionControl ok = ActionControl.create("OK", NullContainer, () -> System.out.println("edit pixelmap OK"));
        ActionControl cancel = ActionControl.create("Cancel", NullContainer, () -> mLogger.fine("Cancel"));
        getEditPixelMapDialog().showDialog(cancel, ok);
    }

    private void generateEdges() {
        ActionControl ok = ActionControl.create("OK", NullContainer, this::generateEdgesOK);
        ActionControl cancel = ActionControl.create("Cancel", NullContainer, () -> mLogger.fine("Cancel"));
        getGenerateEdgesDialog().showDialog(cancel, ok);
    }

    private void generateEdgesOK() {
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ OK");
        SplitTimer.split("generateEdgesOK() start");
        PictureType inputPicture = new PictureType(getColorOOBProperty(), mWidth.getValue(), mHeight.getValue());
        PictureControl inputPictureControl = new PictureControl("Input", "input", NullContainer.NullContainer, inputPicture);
        Perception.getPerception().getRenderService()
                .transform(inputPictureControl, getPreviousTransform(), () -> {
                    setPixelMap(null);
                    regeneratePixelMap(inputPictureControl);
                });
    }

    public synchronized GenerateEdgesDialog getGenerateEdgesDialog() {
        if (mGenerateEdgesDialog == null) {
            mGenerateEdgesDialog = new GenerateEdgesDialog(this, "Canny Edge Transform", "edge");
        }
        return mGenerateEdgesDialog;
    }

    public synchronized EditPixelMapDialog getEditPixelMapDialog() {
        if (mEditPixelMapDialog == null) {
            mEditPixelMapDialog = new EditPixelMapDialog(this, mPixelMap, "Edit PixelMap Dialog", "pixelMapEditor", this);
        }
        return mEditPixelMapDialog;
    }

    //
    // private Color getBackgroundColor(final Point pIn) {
    //
    // final Color color = getColorFromPreviousTransform(pIn);
    // final double fade = mWhiteFade.getDouble();
    // return KColor.fade(color, Color.WHITE, fade);
    // }
    //
    @Override
    public int getHeight() {
        return mHeight.getValue();
    }

    @Override
    public Color getLineColor() {
        return mLineColor.getValue();
    }

    @Override
    public double getLineCurvePreference() {
        return mLineCurvePreference.getValue();
    }

    @Override
    public int getLineEndLengthPercent() {
        return mLineEndLengthPercent.getValue();
    }

    @Override
    public int getLineEndLengthPixel() {
        return mLineEndLengthPercent.getValue();
    }

    @Override
    public LineEndLengthType getLineEndLengthType() {
        return mLineEndLengthType.getValue();
    }

    @Override
    public LineEndShape getLineEndShape() {
        return mLineEndShape.getValue();
    }

    @Override
    public double getLineEndThickness() {
        return mLineEndThickness.getValue();
    }

    @Override
    public double getLineOpacity() {
        return mLineOpacity.getValue();
    }

    @Override
    public double getLineTolerance() {
        return mLineTolerance.getValue();
    }


    @Override
    public int getLongLineLength() {
        return mLongLineLength.getValue();
    }

    @Override
    public double getLongLineThickness() {
        return mLongLineThickness.getValue();
    }

    @Override
    public int getMediumLineLength() {
        return mMediumLineLength.getValue();
    }

    @Override
    public double getMediumLineThickness() {
        return mMediumLineThickness.getValue();
    }

    @Override
    public Color getPixelColor() {
        return mPixelColor.getValue();
    }

    @Override
    public Color getShadowColor() {
        return mShadowColor.getValue();
    }

    @Override
    public double getShadowOpacity() {
        return mShadowOpacity.getValue();
    }

    @Override
    public double getShadowThickness() {
        return mShadowThickness.getValue();
    }

    @Override
    public double getShadowXOffset() {
        return mShadowXOffset.getValue();
    }

    @Override
    public double getShadowYOffset() {
        return mShadowYOffset.getValue();
    }

    @Override
    public int getShortLineLength() {
        return mShortLineLength.getValue();
    }

    @Override
    public double getShortLineThickness() {
        return mShortLineThickness.getValue();
    }

    @Override
    public boolean getShowPixels() {
        return mShowPixels.getValue();
    }

    @Override
    public boolean getShowShadow() {
        return mShowShadow.getValue();
    }


    // @Override
    // public ICUI getUIDynamic(final IControlPrimative<IPicture> pPreviewPictureControl) {
    //
    // return new DynamicUI.HorizontalFlowUISource() //
    // .setLeftUI(DynamicUI.createGrafittiPicture(pPreviewPictureControl) //
    // .setControlContainer(getControlContainer()) //
    // .setPopupMenu(getPopupMenuContainer()) //
    // .setGrafitti(this) //
    // ) //
    // .setRightUI(getControlContainer()) //
    // .createUI();
    // }
    //
    @Override
    public int getWidth() {
        return mWidth.getValue();
    }

    //
    // @Override
    // public void graffiti(final GraphicsHelper pGraphics) {
    // super.graffiti(pGraphics);
    // mGenerateEdgesDialog.graffitiTransform(pGraphics);
    // mEditPixelMapSegmentsDialog.graffitiTransform(pGraphics);
    // }
    //
    // public void postProcess() {
    // getPixelMap().process(this);
    // getPixelMap().setPixelChainDefaultThickness(this);
    // }
    //
    @Override
    public void read(final IPersistDB pDB, final String pId) {
        super.read(pDB, pId);

        // TODO need to change the 360 value to one that is generated from something
        // TODO the width and height should come from the PixelMap ... or it should thrown an error if they are different
        PixelMap pixelMap = new PixelMap(getWidth(), getHeight(), false, this);
        if (pixelMap.canRead(pDB, pId + "." + getPropertyName())) {
            pixelMap.read(pDB, pId + "." + getPropertyName());
            setPixelMap(pixelMap);
        }

        getGenerateEdgesDialog().read(pDB, pId + "." + getPropertyName());
        getEditPixelMapDialog().read(pDB, pId + "." + getPropertyName());
    }

    //
    // public void reapproximate() {
    // getPixelMap().process08_refine();
    // }
    //
    // public void resetHeightWidth() {
    // mWidth.setValue(getPreviousTransform().getWidth());
    // mHeight.setValue(getPreviousTransform().getHeight());
    // }
    //
    // @Override
    // public void setInitialized() {
    // super.setInitialized();
    // if (getUseTransform() && mPixelMap == null) {
    // mGenerateEdgesDialog.generateEdgeImage();
    // }
    // mEditPixelMapSegmentsDialog.setInitialized();
    // mEditPixelMapPixelsDialog.setInitialized();
    // setValues();
    // }
    //
    // public void setPixelMap(final PixelMap pPixelMap) {
    // mPixelMap = pPixelMap;
    // }
    //
    // @Override
    // public void setPreviousTransform(final ITransform pPreviousTransform) {
    // super.setPreviousTransform(pPreviousTransform);
    // mWidth.setValue(super.getWidth());
    // mHeight.setValue(super.getHeight());
    // }
    //
    @Override
    public void setValues() {
        super.setValues();
        mPixelColor.setVisible(mShowPixels.getValue());

        final boolean showMaxiLines = mLinesShow.getValue();
        mLongLineLength.setVisible(showMaxiLines);
        mLongLineThickness.setVisible(showMaxiLines);
        mMediumLineLength.setVisible(showMaxiLines);
        mMediumLineThickness.setVisible(showMaxiLines);
        mShortLineLength.setVisible(showMaxiLines);
        mShortLineThickness.setVisible(showMaxiLines);
        mLineOpacity.setVisible(showMaxiLines);
        mLineColor.setVisible(showMaxiLines);
        mShowShadow.setVisible(showMaxiLines);

        final boolean showShadow = mShowShadow.getValue() && mLinesShow.getValue();
        mShadowColor.setVisible(showShadow);
        mShadowOpacity.setVisible(showShadow);
        mShadowThickness.setVisible(showShadow);
        mShadowXOffset.setVisible(showShadow);
        mShadowYOffset.setVisible(showShadow);

        final boolean isPercent = mLineEndLengthType.getValue() == LineEndLengthType.Percent;
        mLineEndLengthPercent.setVisible(isPercent);
        mLineEndLengthPixels.setVisible(!isPercent);

        final boolean isSquare = mLineEndShape.getValue() == LineEndShape.Square;
        mLineEndLengthType.setEnabled(!isSquare);
        mLineEndLengthPercent.setEnabled(!isSquare);
        mLineEndLengthPixels.setEnabled(!isSquare);
    }

    //
    // @Override
    // public Color transform(final Point pIn) {
    // Color color = getBackgroundColor(pIn);
    // if (mLinesShow.getBoolean()) {
    // color = mPixelMap.transform(pIn, color);
    // }
    // return color;
    // }
    //
    @Override
    public void write(final IPersistDB pDB, final String pId) throws IOException {
        super.write(pDB, pId);

        if (mPixelMap != null) {
            mPixelMap.write(pDB, pId + "." + getPropertyName());
        }

        if (mGenerateEdgesDialog != null) {
            mGenerateEdgesDialog.write(pDB, pId + "." + getPropertyName());
        }

        if (mEditPixelMapDialog != null) {
            mEditPixelMapDialog.write(pDB, pId + "." + getPropertyName());
        }
    }

    @Override
    public void grafitti(final GrafittiHelper pGrafittiHelper) {
        Rectangle r = getGenerateEdgesDialog().getPreviewRectangle();
        pGrafittiHelper.drawRectangle(r, Perception.getPerception().getProperties().getColor1());
    }

    private void regeneratePixelMap(final PictureControl inputPicture) {
        PixelMap pixelMap = null;
        SplitTimer.split("regeneratePixelMap() start");
        ICannyEdgeDetector detector = mGenerateEdgesDialog.createCannyEdgeDetector(CannyEdgeDetectorFactory.Type.DEFAULT);
        try {
            detector.setSourceImage(inputPicture.getValue());
            detector.process(false);

            if (detector.getKeepRunning()) {
                // only set the mData if the detector was allowed to finish
                pixelMap = detector.getEdgeData();
                // mPreviewControl.getValue().setValue(mPreviewPicture);
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ :)");
            }
        } finally {
            if (detector != null) {
                detector.dispose();
            }
        }

        if (pixelMap != null) {
            pixelMap.process();
            setPixelMap(pixelMap);
        }

    }

    public void setPixelMap(PixelMap pPixelMap) {
        mPixelMap = pPixelMap;
        mEditPixelMapDialog = null;
        mEditPixelMapButton.setEnabled(mPixelMap != null);
    }

    private Optional<PixelMap> getPixelMap() {
        return Optional.ofNullable(mPixelMap);
        // TODO need to make all type of mPixelMap Optional<PixelMap>
    }

    @Override
    public void transform(final ITransformResult pRenderResult) {
        Framework.checkParameterNotNull(mLogger, pRenderResult, "pRenderResult");
        getPixelMap().ifPresent(pixelMap -> pixelMap.transform(pRenderResult));
//        int x = (int) Math.floor(getWidth() * pRenderResult.getX());
//        int y = (int) Math.floor(getHeight() * pRenderResult.getY());
//        if (mPixelMap != null && mPixelMap.getPixelAt(x, y).isEdge()) {
//            pRenderResult.setColor(Color.RED);
//        }
    }
}
