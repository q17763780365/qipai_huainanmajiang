package com.anbang.qipai.shouxianmajiang.web.vo;

import java.util.List;
import java.util.Map;

import com.dml.majiang.pai.MajiangPai;
import com.dml.majiang.pai.valueobj.MajiangPaiValueObject;
import com.dml.majiang.player.action.MajiangPlayerAction;
import com.dml.majiang.player.chupaizu.ChichuPaiZu;
import com.dml.majiang.player.chupaizu.GangchuPaiZu;
import com.dml.majiang.player.chupaizu.PengchuPaiZu;
import com.dml.majiang.player.valueobj.MajiangPlayerValueObject;
import com.dml.majiang.position.MajiangPosition;

public class MajiangPlayerValueObjectVO {

    private String id;
    /**
     * 门风
     */
    private MajiangPosition menFeng;
    //剩下的
    private FangruShoupaiListVO fangruShoupaiList;
    /**
     * 公开的牌，不能行牌
     */
    private List<MajiangPai> publicPaiList;

    //可作的动作
    private List<MajiangPlayerAction> actionCandidates;

    private Map<MajiangPai, List<MajiangPai>> hupaiCandidates;

    private List<MajiangPai> kehuCandidates;

    /**
     * 刚摸进待处理的手牌（未放入）
     */
    private MajiangPaiValueObject gangmoShoupai;

    /**
     * 打出的牌
     */
    private List<MajiangPai> dachupaiList;

    private List<ChichuPaiZu> chichupaiZuList;
    private List<PengchuPaiZu> pengchupaiZuList;
    private List<GangchuPaiZu> gangchupaiZuList;

    private boolean watingForMe = false;

    public MajiangPlayerValueObjectVO(MajiangPlayerValueObject majiangPlayerValueObject) {
        id = majiangPlayerValueObject.getId();
        menFeng = majiangPlayerValueObject.getMenFeng();
        fangruShoupaiList = new FangruShoupaiListVO(majiangPlayerValueObject.getFangruShoupaiList(), majiangPlayerValueObject.getFangruGuipaiList(), majiangPlayerValueObject.getTotalShoupaiCount());
        publicPaiList = majiangPlayerValueObject.getPublicPaiList();
        actionCandidates = majiangPlayerValueObject.getActionCandidates();
        if (actionCandidates != null && !actionCandidates.isEmpty()) {
            watingForMe = true;
        }
        hupaiCandidates = majiangPlayerValueObject.getHupaiCandidates();
        kehuCandidates = majiangPlayerValueObject.getKehuCandidates();
        gangmoShoupai = majiangPlayerValueObject.getGangmoShoupai();
        dachupaiList = majiangPlayerValueObject.getDachupaiList();
        chichupaiZuList = majiangPlayerValueObject.getChichupaiZuList();
        pengchupaiZuList = majiangPlayerValueObject.getPengchupaiZuList();
        gangchupaiZuList = majiangPlayerValueObject.getGangchupaiZuList();

    }

    public String getId() {
        return id;
    }

    public MajiangPosition getMenFeng() {
        return menFeng;
    }

    public FangruShoupaiListVO getFangruShoupaiList() {
        return fangruShoupaiList;
    }

    public List<MajiangPai> getPublicPaiList() {
        return publicPaiList;
    }

    public List<MajiangPlayerAction> getActionCandidates() {
        return actionCandidates;
    }

    public MajiangPaiValueObject getGangmoShoupai() {
        return gangmoShoupai;
    }

    public List<MajiangPai> getDachupaiList() {
        return dachupaiList;
    }

    public List<ChichuPaiZu> getChichupaiZuList() {
        return chichupaiZuList;
    }

    public List<PengchuPaiZu> getPengchupaiZuList() {
        return pengchupaiZuList;
    }

    public List<GangchuPaiZu> getGangchupaiZuList() {
        return gangchupaiZuList;
    }

    public boolean isWatingForMe() {
        return watingForMe;
    }

    public Map<MajiangPai, List<MajiangPai>> getHupaiCandidates() {
        return hupaiCandidates;
    }

    public List<MajiangPai> getKehuCandidates() {
        return kehuCandidates;
    }

}
