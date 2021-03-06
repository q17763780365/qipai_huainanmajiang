package com.anbang.qipai.tuidaohu.web.vo;

import com.dml.majiang.pai.MajiangPai;
import com.dml.majiang.pai.valueobj.PaiListValueObject;
import com.dml.majiang.pan.cursor.PaiCursor;
import com.dml.majiang.pan.frame.PanValueObject;

import java.util.ArrayList;
import java.util.List;

public class PanValueObjectVO {

    /**
     * 编号，代表一局中的第几盘
     */
    private int no;

    private List<MajiangPlayerValueObjectVO> playerList;

    private String zhuangPlayerId;

    private PaiListValueObject avaliablePaiList;

    /**
     * 公示的鬼牌集合,不能行牌
     */
    private List<MajiangPai> publicGuipaiList;

    /**
     * 给用户看得到的等待箭头，实际等的不一定是他
     */
    private String publicWaitingPlayerId;

    /**
     * 当前活跃的那张牌的定位
     */
    private PaiCursor activePaiCursor;

    private boolean hasMopai;//有人摸牌
    private String mopaiPlayerId;//摸牌玩家id

    public PanValueObjectVO(PanValueObject panValueObject) {
        no = panValueObject.getNo();
        zhuangPlayerId = panValueObject.getZhuangPlayerId();
        avaliablePaiList = panValueObject.getAvaliablePaiList();
        publicGuipaiList = panValueObject.getPublicGuipaiList();
        publicWaitingPlayerId = panValueObject.getPublicWaitingPlayerId();
        activePaiCursor = panValueObject.getActivePaiCursor();
        playerList = new ArrayList<>();
        panValueObject.getPlayerList()
                .forEach((playerValueObject) -> playerList.add(new MajiangPlayerValueObjectVO(playerValueObject)));
        hasMopai = panValueObject.isHasMopai();
        mopaiPlayerId = panValueObject.getMopaiPlayerId();
    }

    public int getNo() {
        return no;
    }

    public List<MajiangPlayerValueObjectVO> getPlayerList() {
        return playerList;
    }

    public String getZhuangPlayerId() {
        return zhuangPlayerId;
    }

    public PaiListValueObject getAvaliablePaiList() {
        return avaliablePaiList;
    }

    public List<MajiangPai> getPublicGuipaiList() {
        return publicGuipaiList;
    }

    public String getPublicWaitingPlayerId() {
        return publicWaitingPlayerId;
    }

    public PaiCursor getActivePaiCursor() {
        return activePaiCursor;
    }

    public boolean isHasMopai() {
        return hasMopai;
    }

    public String getMopaiPlayerId() {
        return mopaiPlayerId;
    }
}
