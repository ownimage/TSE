/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.type;

import com.ownimage.framework.logging.FrameworkException;
import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.RectangleSize;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.ImageQuality;
import com.ownimage.framework.util.Range2D;
import lombok.NonNull;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Class PictureType represents a picture resource.
 * <p>
 * This resource supports a limited interface to get and set colours based on x,y coordinates of different forms.
 * <p>
 * It also has the bulk operations get/setColours.
 * <p>
 * This also support the ability for the picture to be locked and unlocked so that its owner can prevent unintentional modification.
 * This has been done to make it like the like the other imutable types, but without the overhead of needing to copy the entire
 * object to create a readonly copy.
 */
public class PictureType implements IType<NullMetaType<PictureType>, PictureType>, IPictureSource {

    /**
     * The Constant mLogger.
     */
    private final static Logger mLogger = Framework.getLogger();


    /**
     * The Is360, indicates that the image wraps round on the X axis and so needs to be mapped appropriately.
     */
    private boolean mIs360 = false;

    /**
     * The Lock value. Null indicates that the value is unlocked and hence write operations can be supported. If this contains an
     * Integer the object can only be unlocked with that int.
     */
    private Integer mLock;

    /**
     * The Height of the picture.
     */
    private final int mHeight;

    /**
     * The Width of the picture.
     */
    private final int mWidth;

    /**
     * The raw Image value.
     */
    private final BufferedImage mImage;


    /**
     * Instantiates a new picture type.
     *
     * @param pFile the file that holds the image data.
     * @throws FrameworkException if there is any underlying problem in instantiating the image.
     */
    public PictureType(@NonNull final File pFile) throws FrameworkException {
        Framework.logEntry(mLogger);

        try {
            mImage = ImageIO.read(pFile);
            if (mImage == null) { //
                throw new FrameworkException(this, Level.SEVERE, "Invalid image file: " + pFile, null);
            }

            mLogger.info("PictureType constructor, mImage.getType() = " + mImage.getType());
            mWidth = mImage.getWidth(null);
            mHeight = mImage.getHeight(null);
        } catch (final IOException pEx) {
            final String msg = "Could not open file: " + pFile.getAbsolutePath();
            throw new FrameworkException(this, Level.SEVERE, msg, pEx);
        }

        Framework.logExit(mLogger);
    }

    /**
     * Instantiates a new picture type with the height and width set to the size (i.e. square). The image will be filled with black.
     *
     * @param pSize the height and width
     */
    public PictureType(final int pSize) {
        this(pSize, pSize);
    }

    /**
     * Instantiates a new picture type based on the height and width given. The image will be filled with black.
     *
     * @param pSize the size of the new PictureType
     */
    public PictureType(final RectangleSize pSize) {
        this(pSize.getWidth(), pSize.getHeight());
    }

    /**
     * Instantiates a new picture type based on the height and width given. The image will be filled with black.
     *
     * @param pWidth  the width
     * @param pHeight the height
     */
    public PictureType(final int pWidth, final int pHeight) {
        mImage = new BufferedImage(pWidth, pHeight, BufferedImage.TYPE_INT_ARGB);
        mWidth = pWidth;
        mHeight = pHeight;
    }

    /**
     * Instantiates a new picture type from the filename.
     *
     * @param pFilename the filename
     * @throws FrameworkException if there is any underlying problem in instantiating the image
     */
    public PictureType(final String pFilename) throws FrameworkException {
        this(new File(pFilename));
    }

    public PictureType(@NonNull final PictureType pOrig) {
        Framework.logEntry(mLogger);

        mWidth = pOrig.getWidth();
        mHeight = pOrig.getHeight();
        mImage = new BufferedImage(mWidth, mHeight, BufferedImage.TYPE_INT_ARGB);

        Framework.logExit(mLogger);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.framework.control.type.IType#duplicate()
     */
    @Override
    public PictureType clone() {
        final PictureType picture = new PictureType(getHeight(), getWidth());
        picture.setColors(getColors());
        return picture;
    }

    /**
     * Convert converts a point into the picture resolution. (0.0d, 0.0d) maps to (0.0d, 0.0d), (1.0d, 1.0d) maps to
     * ((double)(mWidth -1), (double)(mHeight - 1)), other points are scaled uniformly. Note that even for 360 images (2.0d, 0.0d)
     * will map to (2.0d x (mWidth -1), 0.0d) and so on.
     *
     * @param pPoint the point
     * @return the point
     */
    public Point convertPictureResolution(final Point pPoint) {
        final double x = pPoint.getX() * (getWidth() - 1);
        final double y = pPoint.getY() * (getHeight() - 1);

        return new Point(x, y);
    }

    /**
     * Convert converts a point into the picture resolution. (0.0d, 0.0d) maps to (0, 0), (1.0d, 1.0d) maps to (mWidth -1,
     * mHeight-1), other points are scaled uniformly. Note that even for 360 images (2.0d, 0.0d) will map to (2 x (mWidth -1), 0)
     * and so on.
     *
     * @param pPoint the point
     * @return the integer point
     */
    public IntegerPoint convertToIntegerPoint(final Point pPoint) {
        final int x = (int) (pPoint.getX() * (getWidth() - 1));
        final int y = (int) (pPoint.getY() * (getHeight() - 1));

        return new IntegerPoint(x, y);
    }

    public PictureType createCompatible() {
        Framework.logEntry(mLogger);
        Framework.logExit(mLogger);
        return new PictureType(this);
    }

    /**
     * This is provided for performance and the resulting image should be used for READ ONLY purposes ONLY.
     */
    public BufferedImage getBufferedImage() {
        return mImage;
    }

    public synchronized Optional<Color> getColor(final double pX, final double pY) {
        final int x = (int) (pX * mWidth);
        final int y = (int) (pY * mHeight);
        return getColor(x, y);
    }

    /**
     * Gets the color at a specific (x, y) coordinate. If outside the image bounds then it will return the out of bounds color.
     *
     * @param pX the x
     * @param pY the y
     * @return the color
     */
    @Override
    public synchronized Optional<Color> getColor(final int pX, final int pY) {
        if (pY < 0 || pY >= getHeight()) {
            return Optional.empty();
        }

        if (!is360() && (pX < 0 || pX >= getWidth())) {
            return Optional.empty();
        }

        int x = pX;

        if (is360()) {
            x = Math.floorMod(x, getWidth());
        }

        if (0 <= x && x < getWidth() && 0 <= pY && pY < getHeight()) {//
            return Optional.of(new Color(mImage.getRGB(x, mHeight - pY - 1)));
        }

        throw new IllegalStateException("x needs to be between 0 and getWidth() and is not, based on x = " + x + ", is360() = "
                + is360() + ", pX = " + pX + " and getWidth() = " + getWidth() + ".");
    }

    /**
     * Gets the color at a specific (x, y) coordinate. If outside the image bounds then it will return the out of bounds color. *
     *
     * @param pPoint the point
     * @return the color
     */
    public Optional<Color> getColor(final IntegerPoint pPoint) {
        if (pPoint == null) {
            throw new IllegalArgumentException("pIn must not be null");
        }

        return getColor(pPoint.getX(), pPoint.getY());
    }

    /**
     * Gets the color at a specific (x, y) coordinate. If outside the image bounds then it will return the out of bounds color. *
     *
     * @param pIn the input point
     * @return the color
     */
    public Optional<Color> getColor(final Point pIn) {
        if (pIn == null) {
            throw new IllegalArgumentException("pIn must not be null");
        }

        final IntegerPoint point = convertToIntegerPoint(pIn);

        return getColor(point);
    }

    /**
     * Gets the colors.
     *
     * @return the colors
     */
    public int[] getColors() {
        return getColors((int[]) null);
    }

    /**
     * Gets the colors.
     *
     * @param pColors the colors
     * @return the colors
     */
    public int[] getColors(final int[] pColors) {
        final int[] array;

        if (pColors != null && pColors.length == mWidth * mHeight) {
            array = pColors;
        } else {
            array = new int[mWidth * mHeight];
        }

        return mImage.getRGB(0, 0, mWidth, mHeight, array, 0, mWidth);
    }

    /**
     * Gets the height.
     *
     * @return the height
     */
    @Override
    public synchronized int getHeight() {
        return mHeight;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.framework.control.type.IType#getMetaModel()
     */
    @Override
    public NullMetaType<PictureType> getMetaModel() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.framework.control.type.IType#getString()
     */
    @Override
    public String getString() {
        throw new UnsupportedOperationException("PictureType does not support getString");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.framework.control.type.IType#getValue()
     */
    @Override
    public PictureType getValue() {
        return this;
    }

    /**
     * Gets the width.
     *
     * @return the width
     */
    @Override
    public synchronized int getWidth() {
        return mWidth;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.framework.util.IPictureIntArray#getColors(int[])
     */

    /**
     * Checks if this is a 360 image.
     *
     * @return true, if is 360
     */
    public boolean is360() {
        return mIs360;
    }

    /**
     * Checks if picture is locked. When locked it is not possible to perform update operations on the picture.
     *
     * @return true, if is locked
     */
    public boolean isLocked() {
        return mLock != null;
    }

    // private boolean inBounds(IntegerPoint pPoint) {
    // return pPoint.getX() >= 0 && pPoint.getX() < getWidth() && pPoint.getY()
    // >= 0 && pPoint.getY() < getHeight();
    // }

    /**
     * Lock the picture. When locked it is not possible to perform update operations on the picture. This can only be unlocked by
     * providing the int returned from this operation.
     *
     * @return the int
     */
    public int lock() {
        testIsLocked();

        final int key = (int) (Math.random() * Integer.MAX_VALUE);
        mLock = key;
        return key;
        // TODO should really use com.perception.util.Lock
    }

    /**
     * Save the image at the file location specified. Note that only jpg and gif formats are supported.
     *
     * @param pFile the file
     * @throws Exception the exception
     */
    public void save(@NonNull final File pFile, final ImageQuality pImageQuality) throws Exception {
        Framework.logEntry(mLogger);
        mLogger.info("mImage.getType() = " + mImage.getType());

        final String fileName = pFile.getName();
        String extension = "";
        final int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }

        if (!("jpg".equals(extension) || "gif".equals(extension))) {
            throw new IllegalArgumentException("Extension of file " + pFile.getAbsolutePath() + " + is ." + extension
                    + ".  It needs to be one of .gif or .jpg.");
        }

        final Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(extension);
        final ImageWriter writer = iter.next();
        final ImageWriteParam iwp = writer.getDefaultWriteParam();

        if ("jpg".equals(extension)) {
            final float quality = pImageQuality.getJPGQuality();
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(quality);
        }

        final BufferedImage imRGB = toBufferedImageOfType(mImage, BufferedImage.TYPE_INT_RGB);

        final FileImageOutputStream outputFile = new FileImageOutputStream(pFile);
        writer.setOutput(outputFile);
        final IIOImage imageX = new IIOImage(imRGB, null, null);
        writer.write(null, imageX, iwp);
        writer.dispose();
        outputFile.close();

        Framework.logExit(mLogger);
    }

    // from https://stackoverflow.com/questions/44182400/how-to-convert-bufferedimage-rgba-to-bufferedimage-rgb
    public static BufferedImage toBufferedImageOfType(final BufferedImage original, final int type) {
        if (original == null) {
            throw new IllegalArgumentException("original == null");
        }
        if (original.getType() == type) {
            return original;
        }
        final BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), type);
        final Graphics2D g = image.createGraphics();
        try {
            g.setComposite(AlphaComposite.Src);
            g.drawImage(original, 0, 0, null);
        } finally {
            g.dispose();
        }
        return image;
    }

    /**
     * Save the file at the location given by pFileName
     *
     * @param pFileName the name
     * @throws Exception the exception
     */
    public void save(@NonNull final String pFileName, final ImageQuality pImageQuality) throws Exception {
        Framework.logEntry(mLogger);

        save(new File(pFileName), pImageQuality);

        Framework.logExit(mLogger);
    }

    /**
     * Sets the 360 nature of the image.
     *
     * @param pIs360 the new 360
     */
    public void set360(final boolean pIs360) {
        testIsLocked();
        mIs360 = pIs360;
    }

    /**
     * Sets the color.
     *
     * @param pX     the x
     * @param pY     the y
     * @param pColor the color
     */
    public synchronized void setColor(final int pX, final int pY, final Color pColor) {
        testIsLocked();

        if (pColor == null) {
            throw new IllegalArgumentException("can't set Color to null");
        }
        if (pY < 0 || pY >= getHeight()) {
            throw new IllegalArgumentException(
                    "pY = " + pY + ", it needs to be between 0 and getHeight()-1 [" + (getHeight() - 1) + "]");
        }

        if (!is360() && (pX < 0 || pX >= getWidth())) {
            throw new IllegalArgumentException(
                    "pX = " + pX + ", it needs to be between 0 and getWidth()-1 [" + (getWidth() - 1) + "]");
        }

        int x = pX;

        if (is360()) {
            x = Math.floorMod(x, getWidth());
        }

        try {
            mImage.setRGB(x, mHeight - pY - 1, pColor.getRGB());
        } catch (final Exception pEx) {
            if (mLogger.isLoggable(Level.SEVERE)) {
                mLogger.severe(pX + " " + pY + " Out of bounds");
            }
            if (mLogger.isLoggable(Level.FINEST)) {
                mLogger.finest(FrameworkLogger.throwableToString(pEx));
            }
            throw pEx;
        }
    }

    /**
     * Sets the color.
     *
     * @param pPoint the point
     * @param pColor the color
     */
    public void setColor(final IntegerPoint pPoint, final Color pColor) {
        testIsLocked();

        if (pPoint == null) {
            throw new IllegalArgumentException("pPoint must not be null");
        }

        if (pColor == null) {
            throw new IllegalArgumentException("pColor must not be null");
        }

        setColor(pPoint.getX(), pPoint.getY(), pColor);
    }

    /**
     * Sets the color.
     *
     * @param pPoint the point
     * @param pColor the color
     */
    public void setColor(final Point pPoint, final Color pColor) {
        testIsLocked();

        if (pPoint == null) {
            throw new IllegalArgumentException("pPoint must not be null");
        }

        final IntegerPoint point = convertToIntegerPoint(pPoint);

        setColor(point, pColor);
    }

    /**
     * Sets the colors.
     *
     * @param pColors the new colors
     */
    public void setColors(final int[] pColors) {
        testIsLocked();

        if (pColors == null) {
            throw new IllegalArgumentException("pColors mst not be null");
        }
        if (pColors.length != getHeight() * getWidth()) {
            throw new IllegalArgumentException(
                    "pColors.length = " + pColors.length + " getWidth() * getHeight() = " + getWidth() * getHeight());
        }

        mImage.setRGB(0, 0, mWidth, mHeight, pColors, 0, mWidth);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.framework.control.type.IType#setString(java.lang.String)
     */
    @Override
    public void setString(final String pValue) {
        throw new UnsupportedOperationException("PictureType does not support setString");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.framework.control.type.IType#setValue(java.lang.Object)
     */
    @Override
    public boolean setValue(final PictureType pValue) {
        throw new UnsupportedOperationException(
                "PictureType does not support setValue.  Use the set methods in the PictureControl.");
    }

    /**
     * Throw exception.
     */
    private void testIsLocked() {
        if (isLocked()) {
            throw new IllegalStateException("Cannot modify PictureType when it is locked.");
        }
    }

    /**
     * Unlock.
     *
     * @param pKey the key
     */
    public void unlock(final int pKey) {

        if (!isLocked()) {
            throw new IllegalStateException("The PictureType is not locked");
        }
        if (mLock != pKey) {
            throw new IllegalArgumentException("Wrong key given to unlock PictureType.");
        }

        mLock = null;
    }

    public void forEach(final BiConsumer<Integer, Integer> pFunction) {
        final Range2D range = new Range2D(getWidth(), getHeight());
        range.forEach(pFunction);
    }

    public void forEachParallel(final BiConsumer<Integer, Integer> pFunction) {
        final Range2D range = new Range2D(getWidth(), getHeight());
        range.forEachParallel(pFunction);
    }

    public void setColor(final BiFunction<Integer, Integer, Color> pFunction) {
        final Range2D range = new Range2D(getWidth(), getHeight());
        range.forEachParallel((x, y) -> setColor(x, y, pFunction.apply(x, y)));
    }

    public void forEachDouble(final BiConsumer<Double, Double> pFunction) {
        final BiConsumer<Integer, Integer> function = (x, y) -> pFunction.accept((double) x / getWidth(), (double) y / getHeight());
        final Range2D range = new Range2D(getWidth(), getHeight());
        range.forEach(function);
    }

    public void forEachDoubleParallel(final BiConsumer<Double, Double> pFunction) {
        final BiConsumer<Integer, Integer> function = (x, y) -> pFunction.accept((double) x / getWidth(), (double) y / getHeight());
        final Range2D range = new Range2D(getWidth(), getHeight());
        range.forEachParallel(function);
    }

    public void setColorDouble(final BiFunction<Double, Double, Color> pFunction) {
        final BiConsumer<Integer, Integer> function = (x, y) -> setColor(x, y, pFunction.apply((double) x / getWidth(), (double) y / getHeight()));
        final Range2D range = new Range2D(getWidth(), getHeight());
        range.forEachParallel(function);
    }

    public RectangleSize getSize() {
        return new RectangleSize(getWidth(), getHeight());
    }
}
