package com.anbang.qipai.admin.plan.bean.signin;

public class SignInPrize {//签到奖品
    private String id;
    private String name;
    private String type;//奖品类型
    private String cardType;
    private int singleNum;//单奖数量
    private int lotteryNum;//已抽取数量
    private int storeNum;//库存数量
    private String iconUrl;
    private Integer prizeProb;//中奖概率
    private Integer firstPrizeProb;//首次中奖概率
    private String overstep;//超出奖池
    private String index;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSingleNum() {
        return singleNum;
    }

    public void setSingleNum(int singleNum) {
        this.singleNum = singleNum;
    }

    public int getLotteryNum() {
        return lotteryNum;
    }

    public void setLotteryNum(int lotteryNum) {
        this.lotteryNum = lotteryNum;
    }

    public int getStoreNum() {
        return storeNum;
    }

    public void setStoreNum(int storeNum) {
        this.storeNum = storeNum;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Integer getPrizeProb() {
        return prizeProb;
    }

    public void setPrizeProb(Integer prizeProb) {
        this.prizeProb = prizeProb;
    }

    public Integer getFirstPrizeProb() {
        return firstPrizeProb;
    }

    public void setFirstPrizeProb(Integer firstPrizeProb) {
        this.firstPrizeProb = firstPrizeProb;
    }

    public String getOverstep() {
        return overstep;
    }

    public void setOverstep(String overstep) {
        this.overstep = overstep;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
