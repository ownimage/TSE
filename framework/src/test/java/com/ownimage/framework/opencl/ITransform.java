package com.ownimage.framework.opencl;

public interface ITransform {

    public void transform();

    public int getId();

    public double[] getDoubleParams();

    public int[] getIntParams();

}
