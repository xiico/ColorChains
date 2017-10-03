package com.colorchains.colorchainsgl;

/**
 * Created by franc on 30/09/2017.
 */

class ProgramInfo {
    private int programId;
    private String fragmentShaderCode;
    private float timeLimit = (float) (2*Math.PI);
    private float timeStep = 0.01666f/2f;

    public int getProgramId() {
        return programId;
    }

    public void setProgramId(int programId) {
        this.programId = programId;
    }

    public String getFragmentShaderCode() {
        return fragmentShaderCode;
    }

    public void setFragmentShaderCode(String fragmentShaderCode) {
        this.fragmentShaderCode = fragmentShaderCode;
    }

    public float getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(float timeLimit) {
        this.timeLimit = timeLimit;
    }

    public float getTimeStep() {
        return timeStep;
    }

    public void setTimeStep(float timeStep) {
        this.timeStep = timeStep;
    }

    public ProgramInfo(int programId, String fragmentShaderCode){
        this.programId = programId;
        this.fragmentShaderCode = fragmentShaderCode;
    }

}
