/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform;

import com.ownimage.framework.control.control.*;
import com.ownimage.framework.control.type.DoubleMetaType;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.SplitTimer;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.app.Services;
import com.ownimage.perception.math.Rectangle;
import com.ownimage.perception.pixelMap.EqualizeValues;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.render.ITransformResult;
import com.ownimage.perception.transform.cannyEdge.CannyEdgeDetectorFactory;
import com.ownimage.perception.transform.cannyEdge.EditPixelMapDialog;
import com.ownimage.perception.transform.cannyEdge.GenerateEdgesDialog;
import com.ownimage.perception.transform.cannyEdge.ICannyEdgeDetector;
import com.ownimage.perception.util.KColor;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import static com.ownimage.framework.control.container.NullContainer.NullContainer;

public class CannyEdgeTransform extends BaseTransform implements IPixelMapTransformSource {

    public enum LineEndLengthType {
        Percent, Pixels
    }

    public enum LineEndShape {
        Square, Straight, Curved
    }


    public final static Logger mLogger = Framework.getLogger();
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

    private final DoubleControl mLineTolerance =
            new DoubleControl("Line Tolerance", "lineTolerance", getContainer(), 1.2d, 0.1d, 10.0d);

    DoubleMetaType meta = new DoubleMetaType(0.1, 100, 5, DoubleMetaType.DisplayType.SPINNER);
    private final DoubleControl mLineCurvePreference =
            new DoubleControl("Curve Preference", "curvePreference", getContainer(), 1.2, meta);//1.2d, 0.1d, 100.0d);

    private final BooleanControl mShowPixels =
            new BooleanControl("Show Pixels", "showPixels", getContainer(), false);

    private final ColorControl mPixelColor =
            new ColorControl("Pixel Colour", "pixelColor", getContainer(), Color.BLACK);

    private final BooleanControl mShowLines =
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

    private final ObjectControl<EqualizeValues> mEqualize =
            new ObjectControl<>(
                    "Equalize",
                    "equalize",
                    getContainer(),
                    EqualizeValues.getDefaultValue(),
                    EqualizeValues.getAllValues());

    // shadow
    private final BooleanControl mShowShadow =
            new BooleanControl("Show Shadow", "showShadow", getContainer(), false);
    private final DoubleControl mShadowXOffset =
            new DoubleControl("Shadow X Offset", "shadowXOffset", getContainer(), 1.0d, -20.0d, 20.0d);

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

    @Override
    public void controlChangeEvent(final Object pControl, final boolean pIsMutating) {
        // mLogger.fine("CannyEdgeTransform:controlChangeEvent " + pControl == null ? "null" : pControl.getDisplayName() + " " +
        // pIsMutating);
        //
        if (isInitialized() && isNotMutating()) {
            try {
                // if (!isInitialized()) { return; }
                //
                // if (pControl == mGeneratePixelMapButton) {
                // mGenerateEdgesDialog.showModalDialog();
                // return;
                // }
                //
                // if (pControl == mEditPixelMapButton) {
                // mLogger.info(() -> "EditPixels");
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
                if ((pControl == mLineTolerance || pControl == mLineCurvePreference) && !pIsMutating) {
                    mPixelMap.reapproximateAllChains();
                }

                if (pControl == mEqualize) equalize();

            } finally {
                super.controlChangeEvent(pControl, pIsMutating);
            }
        }
    }

    private void equalize() {
        try {
            setMutating(true);

            mLogger.info("Equalize");
            getPixelMap().ifPresent(pm -> {
                final EqualizeValues values = mEqualize.getValue();
                pm.equalizeValues(values);
                mShortLineLength.setValue(values.getShortLineLength());
                mMediumLineLength.setValue(values.getMediumLineLength());
                mLongLineLength.setValue(values.getLongLineLength());
                // TODO would be better to pass these three values in ... or pass the EqualizeValues in
                pm.setPixelChainDefaultThickness(this);
                refreshOutputPreview();
            });
        } finally {
            setMutating(false);
        }
    }

    //
    // @Override
    // public ITransformUI createUI() {
    // return GUIFactory.getInstance().createUI((IDynamicTransformUI) this);
    // }
    //
    private void editPixels() {
        getEditPixelMapDialog().showDialog();
        getPerception().refreshOutputPreview();
    }

    private void generateEdges() {
        ActionControl ok = ActionControl.create("OK", NullContainer, this::generateEdgesOK);
        ActionControl cancel = ActionControl.create("Cancel", NullContainer, () -> mLogger.fine("Cancel"));
        getGenerateEdgesDialog().showDialog(cancel, ok);
    }

    private void generateEdgesOK() {
        mLogger.info(() -> "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ OK");
        SplitTimer.split("generateEdgesOK() start");
        PictureType inputPicture = new PictureType(mWidth.getValue(), mHeight.getValue());
        PictureControl inputPictureControl = new PictureControl("Input", "input", NullContainer, inputPicture);
        Services.getServices().getRenderService()
                .getRenderJobBuilder("CannyEdgeTransform::generateEdgesOK", inputPictureControl, getPreviousTransform())
                .withCompleteAction(() -> {
                    setPixelMap(null);
                    regeneratePixelMap(inputPictureControl);
                    equalize();
                })
                .build()
                .run();
    }

    @Override
    public void setPreviousTransform(ITransform pPreviousTransform) {
        try {
            setMutating(true);
            super.setPreviousTransform(pPreviousTransform);
            mWidth.setValue(pPreviousTransform.getWidth());
            mHeight.setValue(pPreviousTransform.getHeight());
        } finally {
            setMutating(false);
        }
        ;
    }

    public synchronized GenerateEdgesDialog getGenerateEdgesDialog() {
        if (mGenerateEdgesDialog == null) {
            mGenerateEdgesDialog = new GenerateEdgesDialog(this, "Canny Edge Transform", "edge");
        }
        return mGenerateEdgesDialog;
    }

    public synchronized EditPixelMapDialog getEditPixelMapDialog() {
        if (mEditPixelMapDialog == null) {
            ActionControl ok = ActionControl.create("OK", NullContainer, () -> mLogger.info(() -> "edit pixelmap OK"));
            ActionControl cancel = ActionControl.create("Cancel", NullContainer, () -> mLogger.fine("Cancel"));
            mEditPixelMapDialog = new EditPixelMapDialog(this, mPixelMap, "Edit PixelMap Dialog", "pixelMapEditor", ok, cancel);
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


    public int getLongLineLength() {
        return mLongLineLength.getValue();
    }

    @Override
    public double getLongLineThickness() {
        return mLongLineThickness.getValue();
    }

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

    @Override
    public boolean getShowLines() {
        return mShowLines.getValue();
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
        final boolean showPixels = mShowPixels.getValue();
        mPixelColor.setVisible(showPixels);

        final boolean showLines = mShowLines.getValue();
        mLineEndShape.setVisible(showLines);
        mLineEndLengthType.setVisible(showLines);
        mLineEndLengthPixels.setVisible(showLines);
        mLineEndLengthPercent.setEnabled(showLines);
        mLineEndThickness.setVisible(showLines);
        mLineColor.setVisible(showLines);
        mLineOpacity.setVisible(showLines);
        mLongLineLength.setVisible(showLines);
        mLongLineThickness.setVisible(showLines);
        mMediumLineLength.setVisible(showLines);
        mMediumLineThickness.setVisible(showLines);
        mShortLineLength.setVisible(showLines);
        mShortLineThickness.setVisible(showLines);
        mShowShadow.setVisible(showLines);
        mEqualize.setVisible(showLines);

        final boolean showShadow = mShowShadow.getValue() && mShowLines.getValue();
        mShadowColor.setVisible(showShadow);
        mShadowOpacity.setVisible(showShadow);
        mShadowThickness.setVisible(showShadow);
        mShadowXOffset.setVisible(showShadow);
        mShadowYOffset.setVisible(showShadow);

        final boolean isSquare = mLineEndShape.getValue() == LineEndShape.Square;
        mLineEndLengthType.setEnabled(!isSquare);
        mLineEndLengthPercent.setEnabled(!isSquare);
        mLineEndLengthPixels.setEnabled(!isSquare);

        final boolean isPercent = mLineEndLengthType.getValue() == LineEndLengthType.Percent;
        mLineEndLengthPercent.setVisible(isPercent && showLines);
        mLineEndLengthPixels.setVisible(!isPercent && showLines);
    }

    //
    // @Override
    // public Color transform(final Point pIn) {
    // Color color = getBackgroundColor(pIn);
    // if (mShowLines.getBoolean()) {
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
        pGrafittiHelper.drawRectangle(r, Services.getServices().getPerception().getProperties().getColor1());
    }

    private void regeneratePixelMap(final PictureControl inputPicture) {
        try {
            getProgressControl().reset();
            getProgressControl().setVisible(true);
            getProgressControl().setProgress("Working ...", 50);

            PixelMap pixelMap = null;
            SplitTimer.split("regeneratePixelMap() start");
            ICannyEdgeDetector detector = mGenerateEdgesDialog.createCannyEdgeDetector(CannyEdgeDetectorFactory.Type.DEFAULT);
            try {
                detector.setSourceImage(inputPicture.getValue());
                detector.process(getProgressControl().reset());

                if (detector.getKeepRunning()) {
                    // only set the mData if the detector was allowed to finish
                    pixelMap = detector.getEdgeData();
                    // mPreviewControl.getValue().setValue(mPreviewPicture);
                    mLogger.info(() -> "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ :)");
                }
            } finally {
                if (detector != null) {
                    detector.dispose();
                }
            }

            if (pixelMap != null) {
                pixelMap.process(getProgressControl().reset());
                setPixelMap(pixelMap);
            }
        } finally {
            getProgressControl().finished();
            getProgressControl().setVisible(false);
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
        float whiteFade = mWhiteFade.getValue().floatValue();
        if (whiteFade != 0.0f) {
            Color color = pRenderResult.getColor();
            Color actual = KColor.fade(color, Color.WHITE, whiteFade);
            pRenderResult.setColor(actual);
        }
    }
}
