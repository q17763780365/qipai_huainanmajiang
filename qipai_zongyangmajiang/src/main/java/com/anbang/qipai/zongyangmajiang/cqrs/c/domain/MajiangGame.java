package com.anbang.qipai.zongyangmajiang.cqrs.c.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.anbang.qipai.zongyangmajiang.cqrs.c.domain.listener.ZongyangMajiangPengGangActionStatisticsListener;
import com.anbang.qipai.zongyangmajiang.cqrs.c.domain.listener.ZongyangMajiangRandomMustHasDongPlayersMenFengDeterminer;
import com.anbang.qipai.zongyangmajiang.cqrs.c.domain.test.ZongyangMajiangFaPaiStrategyTest;
import com.dml.majiang.ju.Ju;
import com.dml.majiang.ju.finish.FixedPanNumbersJuFinishiDeterminer;

import com.dml.majiang.ju.firstpan.HasGuipaiStartFirstPanProcess;
import com.dml.majiang.ju.nextpan.HasGuipaiStartNextPanProcess;
import com.dml.majiang.pan.frame.PanActionFrame;
import com.dml.majiang.pan.publicwaitingplayer.WaitDaPlayerPanPublicWaitingPlayerDeterminer;
import com.dml.majiang.player.action.chi.PengganghuFirstChiActionProcessor;
import com.dml.majiang.player.action.da.DachushoupaiDaActionProcessor;
import com.dml.majiang.player.action.gang.HuFirstBuGangActionProcessor;
import com.dml.majiang.player.action.guo.DoNothingGuoActionProcessor;
import com.dml.majiang.player.action.initial.ZhuangMoPaiInitialActionUpdater;
import com.dml.majiang.player.action.listener.comprehensive.GuoHuBuHuStatisticsListener;
import com.dml.majiang.player.action.listener.comprehensive.GuoPengBuPengStatisticsListener;
import com.dml.majiang.player.action.listener.comprehensive.TianHuAndDihuOpportunityDetector;
import com.dml.majiang.player.action.listener.gang.GuoGangBuGangStatisticsListener;
import com.dml.majiang.player.action.peng.HuFirstBuPengActionProcessor;
import com.dml.majiang.player.zhuang.MenFengDongZhuangDeterminer;
import com.dml.mpgame.game.Finished;
import com.dml.mpgame.game.Playing;
import com.dml.mpgame.game.extend.fpmpv.FixedPlayersMultipanAndVotetofinishGame;
import com.dml.mpgame.game.extend.multipan.WaitingNextPan;
import com.dml.mpgame.game.extend.vote.VoteNotPassWhenPlaying;
import com.dml.mpgame.game.player.GamePlayer;
import com.dml.mpgame.game.player.PlayerPlaying;

public class MajiangGame extends FixedPlayersMultipanAndVotetofinishGame {
    private int panshu;                  //??????
    private int renshu;                  //??????
    private Double difen;                   //??????
    private int powerLimit;
    private OptionalPlay optionalPlay;   //??????????????????
    private Ju ju;
    private Map<String, Double> playeTotalScoreMap = new HashMap<>();
    private Set<String> xipaiPlayerIds = new HashSet<>();
    private String lianmengId;
    private String createplayId;

    /**
     * ??????
     *
     * @param playerId ??????ID
     * @return
     */
    public MajiangGameValueObject xipai(String playerId) {
        xipaiPlayerIds.add(playerId);
        return new MajiangGameValueObject(this);
    }

    /**
     * ???????????????????????????
     *
     * @param currentTime ?????????
     * @return
     * @throws Exception
     */
    public PanActionFrame createJuAndStartFirstPan(long currentTime) throws Exception {
        ju = new Ju();
        ju.setStartFirstPanProcess(new HasGuipaiStartFirstPanProcess());  //???????????????
        ju.setStartNextPanProcess(new HasGuipaiStartNextPanProcess());    //???????????????
        ju.setPlayersMenFengDeterminerForFirstPan(new ZongyangMajiangRandomMustHasDongPlayersMenFengDeterminer(currentTime, createplayId));  //???????????????????????????
        ju.setPlayersMenFengDeterminerForNextPan(new ZongyangMajiangPlayersMenFengDeterminer());                       //????????????????????????
        ju.setZhuangDeterminerForFirstPan(new MenFengDongZhuangDeterminer());   //?????????????????????
        ju.setZhuangDeterminerForNextPan(new MenFengDongZhuangDeterminer());    //?????????????????????
        ju.setAvaliablePaiFiller(new ZongyangMajiangRemoveZhongOrFengFaRandomAvaliablePaiFiller(currentTime + 2, optionalPlay));   //???????????????
        ju.setGuipaiDeterminer(new ZongyangMajiangGuipaiDeterminer(currentTime + 3, true));             //????????????????????????
        ju.setFaPaiStrategy(new ZongyangMajiangFaPaiStrategyTest(13));                            //????????????
        ju.setCurrentPanFinishiDeterminer(new ZongyangMajiangPanFinishiDeterminer());                              //???????????????
        ju.setGouXingPanHu(new ZongyangMajiangGouXingPanHuWithoutQidui());                                                     //???????????????
        ju.setCurrentPanPublicWaitingPlayerDeterminer(new WaitDaPlayerPanPublicWaitingPlayerDeterminer());  //????????????
        ju.setJuFinishiDeterminer(new FixedPanNumbersJuFinishiDeterminer(panshu));                          //?????????????????????
        ju.setJuResultBuilder(new ZongyangMajiangJuResultBuilder());                                               //???????????????
        ju.setInitialActionUpdater(new ZhuangMoPaiInitialActionUpdater());                                  //????????????
        ZongyangMajiangPanResultBuilder tuiDaoHuPanResultBuilder = new ZongyangMajiangPanResultBuilder();                 //???????????????
        tuiDaoHuPanResultBuilder.setDifen(difen);
        tuiDaoHuPanResultBuilder.setOptionalPlay(optionalPlay);
        ju.setCurrentPanResultBuilder(tuiDaoHuPanResultBuilder);
        ZongyangMajiangHuPaiSolutionsTipsFilter tuiDaoHuHuPaiSolutionsTipsFilter = new ZongyangMajiangHuPaiSolutionsTipsFilter();
        tuiDaoHuHuPaiSolutionsTipsFilter.setOptionalPlay(optionalPlay);
        ju.setHupaiPaixingSolutionFilter(tuiDaoHuHuPaiSolutionsTipsFilter);               //????????????

        ju.setMoActionProcessor(new ZongyangMajiangMoActionProcessor());                         //??????????????????
        ju.setMoActionUpdater(new ZongyangMajiangMoActionUpdater());                             //??????????????????
        ju.setDaActionProcessor(new DachushoupaiDaActionProcessor());                     //??????????????????
        ZongyangMajiangDaActionUpdater daUpdater = new ZongyangMajiangDaActionUpdater();                //??????????????????
        daUpdater.setDianpao(false);//????????????
        ju.setDaActionUpdater(daUpdater);
        ju.setChiActionProcessor(new PengganghuFirstChiActionProcessor());                //??????????????????
        ju.setChiActionUpdater(new ZongyangMajiangChiActionUpdater());                           //??????????????????
        ju.setPengActionProcessor(new HuFirstBuPengActionProcessor());                    //??????????????????
        ju.setPengActionUpdater(new ZongyangMajiangPengActionUpdater());                         //??????????????????
        ju.setGangActionProcessor(new HuFirstBuGangActionProcessor());                    //??????????????????
        ZongyangMajiangGangActionUpdater tuiDaoHuGangActionUpdater = new ZongyangMajiangGangActionUpdater();
        ju.setGangActionUpdater(tuiDaoHuGangActionUpdater);                         //??????????????????
        ju.setGuoActionProcessor(new DoNothingGuoActionProcessor());                      //??????????????????
        ju.setGuoActionUpdater(new ZongyangMajiangGuoActionUpdater());                           //??????????????????
        ju.setHuActionProcessor(new ZongyangMajiangHuActionProcessor());                         //??????????????????
        ju.setHuActionUpdater(new ZongyangMajiangClearAllActionHuActionUpdater());               //??????????????????

        ju.addActionStatisticsListener(new ZongyangMajiangPengGangActionStatisticsListener());           //??????????????????
        ju.addActionStatisticsListener(new ZongyangMajiangLastMoActionPlayerRecorder());                 //?????????????????????
        ju.addActionStatisticsListener(new TianHuAndDihuOpportunityDetector());                   //?????????????????????
        ju.addActionStatisticsListener(new GuoHuBuHuStatisticsListener());                        //?????????????????????
        ju.addActionStatisticsListener(new GuoPengBuPengStatisticsListener());                    //?????????????????????
        ju.addActionStatisticsListener(new GuoGangBuGangStatisticsListener());                    //?????????????????????

        // ???????????????
        ju.startFirstPan(allPlayerIds());

        // ???????????????????????????????????????
        return ju.getCurrentPan().findLatestActionFrame();
    }

    /**
     * ??????
     *
     * @param playerId   ??????ID
     * @param actionId   ??????ID
     * @param actionNo   ????????????
     * @param actionTime ????????????
     * @return
     * @throws Exception
     */
    public MajiangActionResult action(String playerId, int actionId, int actionNo, long actionTime) throws Exception {
        PanActionFrame panActionFrame = ju.action(playerId, actionId, actionNo, actionTime);
        MajiangActionResult result = new MajiangActionResult();
        result.setPanActionFrame(panActionFrame);
        if (state.name().equals(VoteNotPassWhenPlaying.name)) {
            state = new Playing();
        }
        if (!xipaiPlayerIds.isEmpty()) {
            xipaiPlayerIds.clear();
        }
        checkAndFinishPan();

        if (state.name().equals(WaitingNextPan.name) || state.name().equals(Finished.name)) { //????????????
            ZongyangMajiangPanResult panResult = (ZongyangMajiangPanResult) ju.findLatestFinishedPanResult();
            for (ZongyangMajiangPanPlayerResult tuiDaoHuPanPlayerResult : panResult.getPanPlayerResultList()) {
                playeTotalScoreMap.put(tuiDaoHuPanPlayerResult.getPlayerId(), tuiDaoHuPanPlayerResult.getTotalScore());
            }
            result.setPanResult(panResult);
            if (state.name().equals(Finished.name)) { //????????????
                result.setJuResult((ZongyangMajiangJuResult) ju.getJuResult());
            }
        }
        result.setMajiangGame(new MajiangGameValueObject(this));
        return result;
    }

    public MajiangActionResult automaticAction(String playerId, int actionId, long actionTime) throws Exception {
        PanActionFrame panActionFrame = ju.automaticAction(playerId, actionId, actionTime);
        MajiangActionResult result = new MajiangActionResult();
        result.setPanActionFrame(panActionFrame);
        if (state.name().equals(VoteNotPassWhenPlaying.name)) {
            state = new Playing();
        }
        checkAndFinishPan();
        if (state.name().equals(WaitingNextPan.name) || state.name().equals(Finished.name)) {// ????????????
            ZongyangMajiangPanResult panResult = (ZongyangMajiangPanResult) ju.findLatestFinishedPanResult();
            for (ZongyangMajiangPanPlayerResult yingjiuzhangPanPlayerResult : panResult.getPanPlayerResultList()) {
                playeTotalScoreMap.put(yingjiuzhangPanPlayerResult.getPlayerId(), yingjiuzhangPanPlayerResult.getTotalScore());
            }
            result.setPanResult(panResult);
            if (state.name().equals(Finished.name)) {// ????????????
                result.setJuResult((ZongyangMajiangJuResult) ju.getJuResult());
            }
        }
        result.setMajiangGame(new MajiangGameValueObject(this));
        return result;
    }


    @Override
    public void start(long currentTime) throws Exception {
        xipaiPlayerIds.clear();
        state = new Playing();
        updateAllPlayersState(new PlayerPlaying());
    }

    @Override
    protected void startNextPan() throws Exception {
        ju.startNextPan();
        state = new Playing();
        updateAllPlayersState(new PlayerPlaying());
    }

    @Override
    public void finish() throws Exception {
        if (ju != null) {
            ju.finish();
        }
    }

    @Override
    protected boolean checkToFinishGame() throws Exception {
        return ju.getJuResult() != null;
    }

    @Override
    protected boolean checkToFinishCurrentPan() throws Exception {
        return ju.getCurrentPan() == null;
    }

    @Override
    protected void updatePlayerToExtendedVotingState(GamePlayer player) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void updateToExtendedVotingState() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void updatePlayerToExtendedVotedState(GamePlayer player) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void recoveryPlayersStateFromExtendedVoting() throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    protected void updateToVoteNotPassStateFromExtendedVoting() throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    public MajiangGameValueObject toValueObject() {
        return new MajiangGameValueObject(this);
    }

    public int getPanshu() {
        return panshu;
    }

    public void setPanshu(int panshu) {
        this.panshu = panshu;
    }

    public int getRenshu() {
        return renshu;
    }

    public void setRenshu(int renshu) {
        this.renshu = renshu;
    }

    public Ju getJu() {
        return ju;
    }

    public void setJu(Ju ju) {
        this.ju = ju;
    }

    public Map<String, Double> getPlayeTotalScoreMap() {
        return playeTotalScoreMap;
    }

    public void setPlayeTotalScoreMap(Map<String, Double> playeTotalScoreMap) {
        this.playeTotalScoreMap = playeTotalScoreMap;
    }

    public Set<String> getXipaiPlayerIds() {
        return xipaiPlayerIds;
    }

    public void setXipaiPlayerIds(Set<String> xipaiPlayerIds) {
        this.xipaiPlayerIds = xipaiPlayerIds;
    }

    public OptionalPlay getOptionalPlay() {
        return optionalPlay;
    }

    public void setOptionalPlay(OptionalPlay optionalPlay) {
        this.optionalPlay = optionalPlay;
    }

    public Double getDifen() {
        return difen;
    }

    public void setDifen(Double difen) {
        this.difen = difen;
    }

    public int getPowerLimit() {
        return powerLimit;
    }

    public void setPowerLimit(int powerLimit) {
        this.powerLimit = powerLimit;
    }

    public String getLianmengId() {
        return lianmengId;
    }

    public void setLianmengId(String lianmengId) {
        this.lianmengId = lianmengId;
    }

    public String getCreateplayId() {
        return createplayId;
    }

    public void setCreateplayId(String createplayId) {
        this.createplayId = createplayId;
    }
}
