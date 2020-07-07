
package com.iita.akilimo.rest.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ComputeRequest {


    @JsonProperty("country")
    private String country;

    @JsonProperty("lat")
    private Double mapLat;

    @JsonProperty("lon")
    private Double mapLong;

    @JsonProperty("area")
    private Double fieldArea;

    @JsonProperty("areaUnits")
    private String areaUnits;

    @JsonProperty("intercrop")
    private String interCroppingType;

    @JsonProperty("IC_MAIZE")
    private boolean interCroppingRec;
    @JsonProperty("FR")
    private boolean fertilizerRec;
    @JsonProperty("PP")
    private boolean plantingPracticesRec;
    @JsonProperty("SPH")
    private boolean scheduledPlantingRec;
    @JsonProperty("SPH")
    private boolean scheduledHarvestRec;

    @JsonProperty("PD")
    private String plantingDate;

    @JsonProperty("HD")
    private String harvestDate;

    @JsonProperty("PD_window")
    private int plantingDateWindow;
    @JsonProperty("HD_window")
    private int harvestDateWindow;

    @JsonProperty("fallowType")
    private String fallowType;

    @JsonProperty("fallowHeight")
    private int fallowHeight;
    @JsonProperty("fallowGreen")
    private boolean fallowGreen;
    @JsonProperty("problemWeeds")
    private boolean problemWeeds;

    @JsonProperty("tractor_plough")
    private boolean tractorPlough;
    @JsonProperty("tractor_harrow")
    private boolean tractorHarrow;
    @JsonProperty("tractor_ridger")
    private boolean tractorRidger;

    @JsonProperty("cost_LMO_areaBasis")
    private double costLmoAreaBasis;
    @JsonProperty("cost_tractor_ploughing")
    private double costTractorPloughing;
    @JsonProperty("cost_tractor_harrowing")
    private double costTractorHarrowing;
    @JsonProperty("cost_tractor_ridging")
    private double costTractorRidging;

    @JsonProperty("cost_manual_ploughing")
    private double costManualPloughing;
    @JsonProperty("cost_manual_harrowing")
    private double costManualHarrowing;
    @JsonProperty("cost_manual_ridging")
    private double costManualRidging;
    @JsonProperty("cost_weeding1")
    private double costWeedingOne;
    @JsonProperty("cost_weeding2")
    private double costWeedingTwo;

    @JsonProperty("ploughing")
    private boolean ploughingDone;
    @JsonProperty("harrowing")
    private boolean harrowingDone;
    @JsonProperty("ridging")
    private boolean ridgingDone;

    @JsonProperty("method_ploughing")
    private String methodPloughing;
    @JsonProperty("method_harrowing")
    private String methodHarrowing;
    @JsonProperty("method_ridging")
    private String methodRidging;

    @JsonProperty("FCY")
    private int currentFieldYield;

    @JsonProperty("CMP")
    private String currentMaizePerformance;
    @JsonProperty("saleSF")
    private Boolean sellToStarchFactory;
    @JsonProperty("nameSF")
    private String starchFactoryName;
    @JsonProperty("cassPD")
    private String cassavaProduceType;

    @JsonProperty("cassUW")
    private int cassavaUnitWeight;
    @JsonProperty("cassUP")
    private double cassavaUnitPrice;

    @JsonProperty("cassUP_m1")
    private double cassUPM1;
    @JsonProperty("cassUP_m2")
    private double cassUPM2;
    @JsonProperty("cassUP_p1")
    private double cassUPP1;
    @JsonProperty("cassUP_p2")
    private double cassUPP2;

    @JsonProperty("maizePD")
    private String maizeProduceType;
    @JsonProperty("maizeUW")
    private int maizeUnitWeight;
    @JsonProperty("maizeUP")
    private double maizeUnitPrice;

    @JsonProperty("maxInv")
    private Double maxInvestment;

    @JsonProperty("SMS")
    private boolean sendSms;
    @JsonProperty("email")
    private boolean sendEmail;

    @JsonProperty("userPhoneCC")
    private String mobileCountryCode;
    @JsonProperty("userPhoneNr")
    private String mobileNumber;
    @JsonProperty("fullPhoneNumber")
    private String fullPhoneNumber;
    @JsonProperty("userName")
    private String userName;
    @JsonProperty("userEmail")
    private String userEmail;
    @JsonProperty("userField")
    private String fieldName;
    @JsonProperty("fieldDesc")
    private String fieldDescription;

    @JsonProperty("riskAtt")
    private int riskAttitude;

    @JsonProperty("ureaavailable")
    private boolean ureaAvailable;
    @JsonProperty("ureaCostperBag")
    private double ureaCostPerBag;
    @JsonProperty("ureaBagWt")
    private int ureaBagWeight;

    @JsonProperty("MOPavailable")
    private boolean mopAvailable;
    @JsonProperty("MOPCostperBag")
    private double mopCostPerBag;
    @JsonProperty("MOPBagWt")
    private int mopBagWeight;

    @JsonProperty("DAPavailable")
    private boolean dapAvailable;
    @JsonProperty("DAPCostperBag")
    private double dapCostPerBag;
    @JsonProperty("DAPBagWt")
    private int dapBagWeight;

    @JsonProperty("TSPavailable")
    private boolean tspAvailable;
    @JsonProperty("TSPCostperBag")
    private double tspCostPerBag;
    @JsonProperty("TSPBagWt")
    private int tspBagWeight;

    @JsonProperty("Nafakaavailable")
    private boolean nafakaAvailable;
    @JsonProperty("NafakaCostperBag")
    private double nafakaCostPerBag;
    @JsonProperty("NafakaBagWt")
    private int nafakaBagWeight;

    @JsonProperty("CANavailable")
    private boolean canAvailable;
    @JsonProperty("CANCostperBag")
    private double canCostPerBag;
    @JsonProperty("CANBagWt")
    private int canBagWeight;

    @JsonProperty("SSPavailable")
    private boolean sspAvailable;
    @JsonProperty("SSPCostperBag")
    private double sspCostPerBag;
    @JsonProperty("SSPBagWt")
    private int sspBagWeight;

    @JsonProperty("NPK201010available")
    private boolean nPK201010Available;
    @JsonProperty("NPK201010CostperBag")
    private double nPK201010CostPerBag;
    @JsonProperty("NPK201010BagWt")
    private int nPK201010BagWeight;

    @JsonProperty("NPK151515available")
    private boolean nPK151515Available;
    @JsonProperty("NPK151515CostperBag")
    private double nPK151515CostPerBag;
    @JsonProperty("NPK151515BagWt")
    private int nPK151515BagWeight;

    @JsonProperty("NPK171717available")
    private boolean npK171717Available;
    @JsonProperty("NPK171717CostperBag")
    private double npK171717CostPerBag;
    @JsonProperty("NPK171717BagWt")
    private int npK171717BagWeight;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Double getMapLat() {
        return mapLat;
    }

    public void setMapLat(Double mapLat) {
        this.mapLat = mapLat;
    }

    public Double getMapLong() {
        return mapLong;
    }

    public void setMapLong(Double mapLong) {
        this.mapLong = mapLong;
    }

    public Double getFieldArea() {
        return fieldArea;
    }

    public void setFieldArea(Double fieldArea) {
        this.fieldArea = fieldArea;
    }

    public String getAreaUnits() {
        return areaUnits;
    }

    public void setAreaUnits(String areaUnits) {
        this.areaUnits = areaUnits;
    }

    public String getInterCroppingType() {
        return interCroppingType;
    }

    public void setInterCroppingType(String interCroppingType) {
        this.interCroppingType = interCroppingType;
    }

    public boolean isInterCroppingRec() {
        return interCroppingRec;
    }

    public void setInterCroppingRec(boolean interCroppingRec) {
        this.interCroppingRec = interCroppingRec;
    }

    public boolean isFertilizerRec() {
        return fertilizerRec;
    }

    public void setFertilizerRec(boolean fertilizerRec) {
        this.fertilizerRec = fertilizerRec;
    }

    public boolean isPlantingPracticesRec() {
        return plantingPracticesRec;
    }

    public void setPlantingPracticesRec(boolean plantingPracticesRec) {
        this.plantingPracticesRec = plantingPracticesRec;
    }

    public boolean isScheduledPlantingRec() {
        return scheduledPlantingRec;
    }

    public void setScheduledPlantingRec(boolean scheduledPlantingRec) {
        this.scheduledPlantingRec = scheduledPlantingRec;
    }

    public boolean isScheduledHarvestRec() {
        return scheduledHarvestRec;
    }

    public void setScheduledHarvestRec(boolean scheduledHarvestRec) {
        this.scheduledHarvestRec = scheduledHarvestRec;
    }

    public String getPlantingDate() {
        return plantingDate;
    }

    public void setPlantingDate(String plantingDate) {
        this.plantingDate = plantingDate;
    }

    public String getHarvestDate() {
        return harvestDate;
    }

    public void setHarvestDate(String harvestDate) {
        this.harvestDate = harvestDate;
    }

    public int getPlantingDateWindow() {
        return plantingDateWindow;
    }

    public void setPlantingDateWindow(int plantingDateWindow) {
        this.plantingDateWindow = plantingDateWindow;
    }

    public int getHarvestDateWindow() {
        return harvestDateWindow;
    }

    public void setHarvestDateWindow(int harvestDateWindow) {
        this.harvestDateWindow = harvestDateWindow;
    }

    public String getFallowType() {
        return fallowType;
    }

    public void setFallowType(String fallowType) {
        this.fallowType = fallowType;
    }

    public int getFallowHeight() {
        return fallowHeight;
    }

    public void setFallowHeight(int fallowHeight) {
        this.fallowHeight = fallowHeight;
    }

    public boolean isFallowGreen() {
        return fallowGreen;
    }

    public void setFallowGreen(boolean fallowGreen) {
        this.fallowGreen = fallowGreen;
    }

    public boolean isProblemWeeds() {
        return problemWeeds;
    }

    public void setProblemWeeds(boolean problemWeeds) {
        this.problemWeeds = problemWeeds;
    }

    public boolean isTractorPlough() {
        return tractorPlough;
    }

    public void setTractorPlough(boolean tractorPlough) {
        this.tractorPlough = tractorPlough;
    }

    public boolean isTractorHarrow() {
        return tractorHarrow;
    }

    public void setTractorHarrow(boolean tractorHarrow) {
        this.tractorHarrow = tractorHarrow;
    }

    public boolean isTractorRidger() {
        return tractorRidger;
    }

    public void setTractorRidger(boolean tractorRidger) {
        this.tractorRidger = tractorRidger;
    }

    public double getCostLmoAreaBasis() {
        return costLmoAreaBasis;
    }

    public void setCostLmoAreaBasis(double costLmoAreaBasis) {
        this.costLmoAreaBasis = costLmoAreaBasis;
    }

    public double getCostTractorPloughing() {
        return costTractorPloughing;
    }

    public void setCostTractorPloughing(double costTractorPloughing) {
        this.costTractorPloughing = costTractorPloughing;
    }

    public double getCostTractorHarrowing() {
        return costTractorHarrowing;
    }

    public void setCostTractorHarrowing(double costTractorHarrowing) {
        this.costTractorHarrowing = costTractorHarrowing;
    }

    public double getCostTractorRidging() {
        return costTractorRidging;
    }

    public void setCostTractorRidging(double costTractorRidging) {
        this.costTractorRidging = costTractorRidging;
    }

    public double getCostManualPloughing() {
        return costManualPloughing;
    }

    public void setCostManualPloughing(double costManualPloughing) {
        this.costManualPloughing = costManualPloughing;
    }

    public double getCostManualHarrowing() {
        return costManualHarrowing;
    }

    public void setCostManualHarrowing(double costManualHarrowing) {
        this.costManualHarrowing = costManualHarrowing;
    }

    public double getCostManualRidging() {
        return costManualRidging;
    }

    public void setCostManualRidging(double costManualRidging) {
        this.costManualRidging = costManualRidging;
    }

    public double getCostWeedingOne() {
        return costWeedingOne;
    }

    public void setCostWeedingOne(double costWeedingOne) {
        this.costWeedingOne = costWeedingOne;
    }

    public double getCostWeedingTwo() {
        return costWeedingTwo;
    }

    public void setCostWeedingTwo(double costWeedingTwo) {
        this.costWeedingTwo = costWeedingTwo;
    }

    public boolean isPloughingDone() {
        return ploughingDone;
    }

    public void setPloughingDone(boolean ploughingDone) {
        this.ploughingDone = ploughingDone;
    }

    public boolean isHarrowingDone() {
        return harrowingDone;
    }

    public void setHarrowingDone(boolean harrowingDone) {
        this.harrowingDone = harrowingDone;
    }

    public boolean isRidgingDone() {
        return ridgingDone;
    }

    public void setRidgingDone(boolean ridgingDone) {
        this.ridgingDone = ridgingDone;
    }

    public String getMethodPloughing() {
        return methodPloughing;
    }

    public void setMethodPloughing(String methodPloughing) {
        this.methodPloughing = methodPloughing;
    }

    public String getMethodHarrowing() {
        return methodHarrowing;
    }

    public void setMethodHarrowing(String methodHarrowing) {
        this.methodHarrowing = methodHarrowing;
    }

    public String getMethodRidging() {
        return methodRidging;
    }

    public void setMethodRidging(String methodRidging) {
        this.methodRidging = methodRidging;
    }

    public int getCurrentFieldYield() {
        return currentFieldYield;
    }

    public void setCurrentFieldYield(int currentFieldYield) {
        this.currentFieldYield = currentFieldYield;
    }

    public String getCurrentMaizePerformance() {
        return currentMaizePerformance;
    }

    public void setCurrentMaizePerformance(String currentMaizePerformance) {
        this.currentMaizePerformance = currentMaizePerformance;
    }

    public Boolean getSellToStarchFactory() {
        return sellToStarchFactory;
    }

    public void setSellToStarchFactory(Boolean sellToStarchFactory) {
        this.sellToStarchFactory = sellToStarchFactory;
    }

    public String getStarchFactoryName() {
        return starchFactoryName;
    }

    public void setStarchFactoryName(String starchFactoryName) {
        this.starchFactoryName = starchFactoryName;
    }

    public String getCassavaProduceType() {
        return cassavaProduceType;
    }

    public void setCassavaProduceType(String cassavaProduceType) {
        this.cassavaProduceType = cassavaProduceType;
    }

    public int getCassavaUnitWeight() {
        return cassavaUnitWeight;
    }

    public void setCassavaUnitWeight(int cassavaUnitWeight) {
        this.cassavaUnitWeight = cassavaUnitWeight;
    }

    public double getCassavaUnitPrice() {
        return cassavaUnitPrice;
    }

    public void setCassavaUnitPrice(double cassavaUnitPrice) {
        this.cassavaUnitPrice = cassavaUnitPrice;
    }

    public double getCassUPM1() {
        return cassUPM1;
    }

    public void setCassUPM1(double cassUPM1) {
        this.cassUPM1 = cassUPM1;
    }

    public double getCassUPM2() {
        return cassUPM2;
    }

    public void setCassUPM2(double cassUPM2) {
        this.cassUPM2 = cassUPM2;
    }

    public double getCassUPP1() {
        return cassUPP1;
    }

    public void setCassUPP1(double cassUPP1) {
        this.cassUPP1 = cassUPP1;
    }

    public double getCassUPP2() {
        return cassUPP2;
    }

    public void setCassUPP2(double cassUPP2) {
        this.cassUPP2 = cassUPP2;
    }

    public String getMaizeProduceType() {
        return maizeProduceType;
    }

    public void setMaizeProduceType(String maizeProduceType) {
        this.maizeProduceType = maizeProduceType;
    }

    public int getMaizeUnitWeight() {
        return maizeUnitWeight;
    }

    public void setMaizeUnitWeight(int maizeUnitWeight) {
        this.maizeUnitWeight = maizeUnitWeight;
    }

    public double getMaizeUnitPrice() {
        return maizeUnitPrice;
    }

    public void setMaizeUnitPrice(double maizeUnitPrice) {
        this.maizeUnitPrice = maizeUnitPrice;
    }

    public Double getMaxInvestment() {
        return maxInvestment;
    }

    public void setMaxInvestment(Double maxInvestment) {
        this.maxInvestment = maxInvestment;
    }

    public boolean isSendSms() {
        return sendSms;
    }

    public void setSendSms(boolean sendSms) {
        this.sendSms = sendSms;
    }

    public boolean isSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public String getMobileCountryCode() {
        return mobileCountryCode;
    }

    public void setMobileCountryCode(String mobileCountryCode) {
        this.mobileCountryCode = mobileCountryCode;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getFullPhoneNumber() {
        return fullPhoneNumber;
    }

    public void setFullPhoneNumber(String fullPhoneNumber) {
        this.fullPhoneNumber = fullPhoneNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldDescription() {
        return fieldDescription;
    }

    public void setFieldDescription(String fieldDescription) {
        this.fieldDescription = fieldDescription;
    }

    public int getRiskAttitude() {
        return riskAttitude;
    }

    public void setRiskAttitude(int riskAttitude) {
        this.riskAttitude = riskAttitude;
    }

    public boolean isUreaAvailable() {
        return ureaAvailable;
    }

    public void setUreaAvailable(boolean ureaAvailable) {
        this.ureaAvailable = ureaAvailable;
    }

    public double getUreaCostPerBag() {
        return ureaCostPerBag;
    }

    public void setUreaCostPerBag(double ureaCostPerBag) {
        this.ureaCostPerBag = ureaCostPerBag;
    }

    public int getUreaBagWeight() {
        return ureaBagWeight;
    }

    public void setUreaBagWeight(int ureaBagWeight) {
        this.ureaBagWeight = ureaBagWeight;
    }

    public boolean isMopAvailable() {
        return mopAvailable;
    }

    public void setMopAvailable(boolean mopAvailable) {
        this.mopAvailable = mopAvailable;
    }

    public double getMopCostPerBag() {
        return mopCostPerBag;
    }

    public void setMopCostPerBag(double mopCostPerBag) {
        this.mopCostPerBag = mopCostPerBag;
    }

    public int getMopBagWeight() {
        return mopBagWeight;
    }

    public void setMopBagWeight(int mopBagWeight) {
        this.mopBagWeight = mopBagWeight;
    }

    public boolean isDapAvailable() {
        return dapAvailable;
    }

    public void setDapAvailable(boolean dapAvailable) {
        this.dapAvailable = dapAvailable;
    }

    public double getDapCostPerBag() {
        return dapCostPerBag;
    }

    public void setDapCostPerBag(double dapCostPerBag) {
        this.dapCostPerBag = dapCostPerBag;
    }

    public int getDapBagWeight() {
        return dapBagWeight;
    }

    public void setDapBagWeight(int dapBagWeight) {
        this.dapBagWeight = dapBagWeight;
    }

    public boolean isTspAvailable() {
        return tspAvailable;
    }

    public void setTspAvailable(boolean tspAvailable) {
        this.tspAvailable = tspAvailable;
    }

    public double getTspCostPerBag() {
        return tspCostPerBag;
    }

    public void setTspCostPerBag(double tspCostPerBag) {
        this.tspCostPerBag = tspCostPerBag;
    }

    public int getTspBagWeight() {
        return tspBagWeight;
    }

    public void setTspBagWeight(int tspBagWeight) {
        this.tspBagWeight = tspBagWeight;
    }

    public boolean isNafakaAvailable() {
        return nafakaAvailable;
    }

    public void setNafakaAvailable(boolean nafakaAvailable) {
        this.nafakaAvailable = nafakaAvailable;
    }

    public double getNafakaCostPerBag() {
        return nafakaCostPerBag;
    }

    public void setNafakaCostPerBag(double nafakaCostPerBag) {
        this.nafakaCostPerBag = nafakaCostPerBag;
    }

    public int getNafakaBagWeight() {
        return nafakaBagWeight;
    }

    public void setNafakaBagWeight(int nafakaBagWeight) {
        this.nafakaBagWeight = nafakaBagWeight;
    }

    public boolean isCanAvailable() {
        return canAvailable;
    }

    public void setCanAvailable(boolean canAvailable) {
        this.canAvailable = canAvailable;
    }

    public double getCanCostPerBag() {
        return canCostPerBag;
    }

    public void setCanCostPerBag(double canCostPerBag) {
        this.canCostPerBag = canCostPerBag;
    }

    public int getCanBagWeight() {
        return canBagWeight;
    }

    public void setCanBagWeight(int canBagWeight) {
        this.canBagWeight = canBagWeight;
    }

    public boolean isSspAvailable() {
        return sspAvailable;
    }

    public void setSspAvailable(boolean sspAvailable) {
        this.sspAvailable = sspAvailable;
    }

    public double getSspCostPerBag() {
        return sspCostPerBag;
    }

    public void setSspCostPerBag(double sspCostPerBag) {
        this.sspCostPerBag = sspCostPerBag;
    }

    public int getSspBagWeight() {
        return sspBagWeight;
    }

    public void setSspBagWeight(int sspBagWeight) {
        this.sspBagWeight = sspBagWeight;
    }

    public boolean isnPK201010Available() {
        return nPK201010Available;
    }

    public void setnPK201010Available(boolean nPK201010Available) {
        this.nPK201010Available = nPK201010Available;
    }

    public double getnPK201010CostPerBag() {
        return nPK201010CostPerBag;
    }

    public void setnPK201010CostPerBag(double nPK201010CostPerBag) {
        this.nPK201010CostPerBag = nPK201010CostPerBag;
    }

    public int getnPK201010BagWeight() {
        return nPK201010BagWeight;
    }

    public void setnPK201010BagWeight(int nPK201010BagWeight) {
        this.nPK201010BagWeight = nPK201010BagWeight;
    }

    public boolean isnPK151515Available() {
        return nPK151515Available;
    }

    public void setnPK151515Available(boolean nPK151515Available) {
        this.nPK151515Available = nPK151515Available;
    }

    public double getnPK151515CostPerBag() {
        return nPK151515CostPerBag;
    }

    public void setnPK151515CostPerBag(double nPK151515CostPerBag) {
        this.nPK151515CostPerBag = nPK151515CostPerBag;
    }

    public int getnPK151515BagWeight() {
        return nPK151515BagWeight;
    }

    public void setnPK151515BagWeight(int nPK151515BagWeight) {
        this.nPK151515BagWeight = nPK151515BagWeight;
    }

    public boolean isNpK171717Available() {
        return npK171717Available;
    }

    public void setNpK171717Available(boolean npK171717Available) {
        this.npK171717Available = npK171717Available;
    }

    public double getNpK171717CostPerBag() {
        return npK171717CostPerBag;
    }

    public void setNpK171717CostPerBag(double npK171717CostPerBag) {
        this.npK171717CostPerBag = npK171717CostPerBag;
    }

    public int getNpK171717BagWeight() {
        return npK171717BagWeight;
    }

    public void setNpK171717BagWeight(int npK171717BagWeight) {
        this.npK171717BagWeight = npK171717BagWeight;
    }
}