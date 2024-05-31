package org.apache.streampipes.model.loadbalancer;

public class ServiceUsageReport {

  private Usage cpu;

  private Usage memory;


  private Usage bandwidthIn;

  private Usage bandwidthOut;

  public long lastUpdate;

  public ServiceUsageReport() {
    bandwidthIn=new Usage();
    bandwidthOut=new Usage();
  }

  public Usage getCpu() {
    return cpu;
  }

  public void setCpu(Usage cpu) {
    this.cpu = cpu;
  }

  public Usage getMemory() {
    return memory;
  }

  public void setMemory(Usage memory) {
    this.memory = memory;
  }

  public Usage getBandwidthIn() {
    return bandwidthIn;
  }

  public void setBandwidthIn(Usage bandwidthIn) {
    this.bandwidthIn = bandwidthIn;
  }

  public Usage getBandwidthOut() {
    return bandwidthOut;
  }

  public void setBandwidthOut(Usage bandwidthOut) {
    this.bandwidthOut = bandwidthOut;
  }

  public long getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(long lastUpdate) {
    this.lastUpdate = lastUpdate;
  }

  @Override
  public String toString() {
    return "ServiceUsageReport{" +
            "cpu=" + cpu +
            ", memory=" + memory +
            ", bandwidthIn=" + bandwidthIn +
            ", bandwidthOut=" + bandwidthOut +
            ", lastUpdate=" + lastUpdate +
            '}';
  }

}
