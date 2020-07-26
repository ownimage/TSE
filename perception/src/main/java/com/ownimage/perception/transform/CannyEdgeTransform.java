/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform;

import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.ColorControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.control.control.IAction;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.ObjectControl;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.type.DoubleMetaType;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.math.Rectangle;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.undo.UndoRedoAction;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.KColor;
import com.ownimage.framework.util.SplitTimer;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.app.Services;
import com.ownimage.perception.pixelMap.EqualizeValues;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMapData;
import com.ownimage.perception.pixelMap.services.Config;
import com.ownimage.perception.pixelMap.services.PixelChainService;
import com.ownimage.perception.pixelMap.services.PixelMapActionService;
import com.ownimage.perception.pixelMap.services.PixelMapApproximationService;
import com.ownimage.perception.pixelMap.services.PixelMapService;
import com.ownimage.perception.pixelMap.services.PixelMapTransformService;
import com.ownimage.perception.render.ITransformResult;
import com.ownimage.perception.transform.cannyEdge.CannyEdgeDetectorFactory;
import com.ownimage.perception.transform.cannyEdge.EditPixelMapDialog;
import com.ownimage.perception.transform.cannyEdge.GenerateEdgesDialog;
import com.ownimage.perception.transform.cannyEdge.ICannyEdgeDetector;
import lombok.NonNull;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ownimage.framework.control.container.NullContainer.NullContainer;

public class CannyEdgeTransform extends BaseTransform implements IPixelMapTransformSource {

    private static ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private static PixelMapService pixelMapService = context.getBean(PixelMapService.class);
    private static PixelMapApproximationService pixelMapApproximationService = context.getBean(PixelMapApproximationService.class);
    private static PixelMapActionService pixelMapActionService = context.getBean(PixelMapActionService.class);
    private static PixelMapTransformService pixelMapTransformService = context.getBean(PixelMapTransformService.class);

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

    private GenerateEdgesDialog mGenerateEdgesDialog;
    private EditPixelMapDialog mEditPixelMapDialog;

    private final DoubleControl mWhiteFade =
            new DoubleControl("White Fade", "whiteFade", getContainer(), 0.0d);

    private final DoubleControl mLineTolerance =
            new DoubleControl("Line Tolerance", "lineTolerance", getContainer(), 1.2d, 0.1d, 10.0d);

    private final DoubleMetaType meta = new DoubleMetaType(0.1, 100, 5, DoubleMetaType.DisplayType.SPINNER);
    private final DoubleControl mLineCurvePreference =
            new DoubleControl("Curve Preference", "curvePreference", getContainer(), 1.2, meta);//1.2d, 0.1d, 100.0d);

    private final BooleanControl mShowPixels =
            new BooleanControl("Show Pixels", "showPixels", getContainer(), false);

    private final ColorControl mPixelColor =
            new ColorControl("Pixel Colour", "pixelColor", getContainer(), Color.BLACK);

    private final BooleanControl mShowLines =
            new BooleanControl("Show Lines", "showLines", getContainer(), true);

    private final ObjectControl<LineEndShape> mLineEndShape =
            new ObjectControl<>("Line End Shape", "lineEndShape", getContainer(), LineEndShape.Curved, LineEndShape.values());

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
            new DoubleControl("Long Line Thickness", "longLineThickness", getContainer(), 1.5d, 0.0d, 10.0d);
    private final IntegerControl mMediumLineLength =
            new IntegerControl("Medium Line Length", "mediumLineLength", getContainer(), 0, 0, 1000, 20);
    private final DoubleControl mMediumLineThickness =
            new DoubleControl("Medium Line Thickness", "mediumLineThickness", getContainer(), 1.0d, 0.0d, 10.0d);

    private final IntegerControl mShortLineLength =
            new IntegerControl("Short Line Length", "shortLineLength", getContainer(), 0, 0, 1000, 20);
    private final DoubleControl mShortLineThickness =
            new DoubleControl("Short Line Thickness", "shortLineThickness", getContainer(), 0.5, 0.0d, 10.0d);

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

    private ImmutablePixelMapData mPixelMap; // this is the picture from the file processed for edges

    public CannyEdgeTransform(Perception pPerception) {
        super("Canny Edge", "cannyEdge");
    }

    @Override
    public void controlChangeEvent(IControl pControl, boolean pIsMutating) {
        Framework.log(mLogger, Level.FINE, () -> "CannyEdgeTransform:controlChangeEvent " + pControl == null ? "null" : pControl + " " + pIsMutating);
        if (pControl != null) {
            Framework.log(mLogger, Level.FINE, () -> "pControl.getDisplayName() = " + pControl.getDisplayName());
        }

        if (isInitialized() && isNotMutating()) {
            try {
                setMutating(true);
                Framework.log(mLogger, Level.FINE, () -> "Running controlChangeEvent");

                if (pControl.isOneOf(mShortLineLength, mMediumLineLength, mLongLineLength)) {
                    if (getPixelMap().isPresent()) {
                        setPixelMap(pixelMapActionService.actionSetPixelChainDefaultThickness(getPixelMap().get(), this));
                    }
                    mEqualize.setValue(EqualizeValues.getDefaultValue());
                }

                if (pControl == mLineTolerance && !pIsMutating) {
                    setPixelMap(pixelMapActionService.actionReapproximate(mPixelMap, this));
                }

                if (pControl == mLineCurvePreference && !pIsMutating) {
                    setPixelMap(pixelMapActionService.actionRerefine(mPixelMap, this));
                }

                if (pControl == mEqualize) {
                    equalize();
                }
            } finally {
                setMutating(false);
                super.controlChangeEvent(pControl, pIsMutating);
            }
        }
    }

    private void equalize() {
        try {
            setMutating(true);

            mLogger.info("Equalize");
            getPixelMap().ifPresent(pm -> {
                EqualizeValues values = mEqualize.getValue();
                setPixelMap(pixelMapActionService.actionEqualizeValues(pm, values));
                mShortLineLength.setValue(values.getShortLineLength());
                mMediumLineLength.setValue(values.getMediumLineLength());
                mLongLineLength.setValue(values.getLongLineLength());
                // TODO would be better to pass these three values in ... or pass the EqualizeValues in
                setPixelMap(pixelMapActionService.actionSetPixelChainDefaultThickness(pm, this));
                refreshOutputPreview();
            });
        } finally {
            setMutating(false);
        }
    }

    private void editPixels() {
        var undoPixelMap = getPixelMap().orElseThrow();
        val redoPixelMap = getEditPixelMapDialog().showDialog();
        if (redoPixelMap != undoPixelMap) {
            getUndoRedoBuffer().add(new UndoRedoAction("Edit Pixels Dialog"
                    , () -> setPixelMap(redoPixelMap)
                    , () -> setPixelMap(undoPixelMap)));
            setPixelMap(redoPixelMap);
            refreshOutputPreview();
        }
    }

    private void generateEdges() {
        val undo = mPixelMap;
        mPixelMap = null;
        setGenEditPixelMapButtonState(false);
        val okControl = ActionControl.create("OK", NullContainer, this::generateEdgesOK);
        IAction cancelActon = () -> {
            mPixelMap = undo;
            setGenEditPixelMapButtonState(mPixelMap != null);
        };
        val cancelControl = ActionControl.create("Cancel", NullContainer, cancelActon);
        getGenerateEdgesDialog().showDialog(cancelControl, okControl, cancelActon);
    }

    private void setGenEditPixelMapButtonState(boolean pState) {
        mEditPixelMapButton.setEnabled(pState);
        mGeneratePixelMapButton.setEnabled(pState);
    }

    private void generateEdgesOK() {
        setGenEditPixelMapButtonState(false);
        mLogger.info(() -> "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ OK");
        SplitTimer.split("generateEdgesOK() start");
        mPixelMap = null;
        PictureType inputPicture = new PictureType(mWidth.getValue(), mHeight.getValue());
        PictureControl inputPictureControl = new PictureControl("Input", "input", NullContainer, inputPicture);
        Services.getServices().getRenderService()
                .getRenderJobBuilder("CannyEdgeTransform::generateEdgesOK", inputPictureControl, getPreviousTransform())
                .withCompleteAction(() -> {
                    regeneratePixelMap(inputPictureControl);
                    equalize();
                    setGenEditPixelMapButtonState(true);
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
    }

    private synchronized GenerateEdgesDialog getGenerateEdgesDialog() {
        if (mGenerateEdgesDialog == null) {
            mGenerateEdgesDialog = new GenerateEdgesDialog(this, "Canny Edge Transform", "edge");
        }
        return mGenerateEdgesDialog;
    }

    private synchronized EditPixelMapDialog getEditPixelMapDialog() {
        if (mEditPixelMapDialog == null) {
            mEditPixelMapDialog = new EditPixelMapDialog(this);
        }
        return mEditPixelMapDialog;
    }

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

    @Override
    public int getWidth() {
        return mWidth.getValue();
    }

    @Override
    public void read(IPersistDB pDB, String pId) {
        super.read(pDB, pId);
        // TODO need to move width, height and 360 out of the transform and into the pixelmap

        // TODO need to change the 360 value to one that is generated from something
        // TODO the width and height should come from the PixelMap ... or it should thrown an error if they are different
        ImmutablePixelMapData pixelMap = ImmutablePixelMapData.builder().width(getWidth()).height(getHeight()).build();
        if (pixelMapService.canRead(pDB, pId + "." + getPropertyName())) {
            //pixelMap.read(pDB, pId + "." + getPropertyName());
            pixelMap = pixelMapService.read(pDB, pId + "." + getPropertyName(), this);
            setPixelMap(pixelMap);
        }

        getGenerateEdgesDialog().read(pDB, pId + "." + getPropertyName());
        getEditPixelMapDialog().read(pDB, pId + "." + getPropertyName());
    }

    @Override
    public void setValues() {
        super.setValues();
        boolean showPixels = mShowPixels.getValue();
        mPixelColor.setVisible(showPixels);

        boolean showLines = mShowLines.getValue();
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

        boolean showShadow = mShowShadow.getValue() && mShowLines.getValue();
        mShadowColor.setVisible(showShadow);
        mShadowOpacity.setVisible(showShadow);
        mShadowThickness.setVisible(showShadow);
        mShadowXOffset.setVisible(showShadow);
        mShadowYOffset.setVisible(showShadow);

        boolean isSquare = mLineEndShape.getValue() == LineEndShape.Square;
        mLineEndLengthType.setEnabled(!isSquare);
        mLineEndLengthPercent.setEnabled(!isSquare);
        mLineEndLengthPixels.setEnabled(!isSquare);

        boolean isPercent = mLineEndLengthType.getValue() == LineEndLengthType.Percent;
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
    public void write(IPersistDB pDB, String pId) throws IOException {
        super.write(pDB, pId);

        if (mPixelMap != null) {
            pixelMapService.write(mPixelMap, pDB, pId + "." + getPropertyName());
        }

        if (mGenerateEdgesDialog != null) {
            mGenerateEdgesDialog.write(pDB, pId + "." + getPropertyName());
        }

        if (mEditPixelMapDialog != null) {
            mEditPixelMapDialog.write(pDB, pId + "." + getPropertyName());
        }
    }

    @Override
    public void graffiti(GrafittiHelper pGrafittiHelper) {
        Rectangle r = getGenerateEdgesDialog().getPreviewRectangle();
        pGrafittiHelper.drawRectangle(r, Services.getServices().getPerception().getProperties().getColor1());
    }

    private void regeneratePixelMap(PictureControl inputPicture) {
        try {
            getProgressControl().reset();
            getProgressControl().setVisible(true);
            getProgressControl().setProgress("Working ...", 50);

            ImmutablePixelMapData pixelMap = null;
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
                pixelMap = pixelMapApproximationService.actionProcess(pixelMap, this, getProgressControl().reset());
                setPixelMap(pixelMap);
            }
        } finally {
            getProgressControl().finished();
            getProgressControl().setVisible(false);
        }
    }

    public Optional<ImmutablePixelMapData> getPixelMap() {
        return Optional.ofNullable(mPixelMap);
    }

    public void setPixelMap(ImmutablePixelMapData pPixelMap) {
        if (mPixelMap != null && (pPixelMap.width() != getWidth() || pPixelMap.height() != getHeight())) {
            throw new IllegalArgumentException("pPixelMap width and height must match existing PixelMap is present.");
        }

        if (mEditPixelMapDialog != null && mEditPixelMapDialog.getPixelMap() != pPixelMap) {
            mEditPixelMapDialog = null;
        }
        mPixelMap = pPixelMap;
        setGenEditPixelMapButtonState(true);
    }

    @Override
    public void transform(@NonNull ITransformResult pRenderResult) {
            getPixelMap().ifPresent(pixelMap -> pixelMapTransformService.transform(pixelMap, this, pRenderResult));

            float whiteFade = mWhiteFade.getValue().floatValue();
            if (whiteFade != 0.0f) {
                Color color = pRenderResult.getColor();
                Color actual = KColor.fade(color, Color.WHITE, whiteFade);
                pRenderResult.setColor(actual);
            }
    }

}
