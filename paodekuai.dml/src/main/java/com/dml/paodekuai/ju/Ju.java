package com.dml.paodekuai.ju;

import java.util.*;

import com.dml.paodekuai.player.PlayerNotFoundException;
import com.dml.paodekuai.wanfa.OptionalPlay;
import com.dml.puke.pai.PukePai;
import com.dml.puke.pai.PukePaiMian;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.dml.paodekuai.gameprocess.CurrentPanFinishiDeterminer;
import com.dml.paodekuai.gameprocess.JuFinishiDeterminer;
import com.dml.paodekuai.pai.waihao.WaihaoGenerator;
import com.dml.paodekuai.pan.CurrentPanResultBuilder;
import com.dml.paodekuai.pan.Pan;
import com.dml.paodekuai.pan.PanActionFrame;
import com.dml.paodekuai.pan.PanResult;
import com.dml.paodekuai.pan.PanValueObject;
import com.dml.paodekuai.player.PaodekuaiPlayer;
import com.dml.paodekuai.player.action.ActionStatisticsListenerManager;
import com.dml.paodekuai.player.action.da.AllKedaPaiSolutionsGenerator;
import com.dml.paodekuai.player.action.da.DaAction;
import com.dml.paodekuai.player.action.da.DaActionStatisticsListener;
import com.dml.paodekuai.player.action.da.KedaPaiSolutionsForTipsGenerator;
import com.dml.paodekuai.player.action.da.KeyaDaPaiDianShuSolutionsGenerator;
import com.dml.paodekuai.player.action.da.YaPaiSolutionsTipsFilter;
import com.dml.paodekuai.player.action.da.solution.DianShuZuYaPaiSolutionCalculator;
import com.dml.paodekuai.player.action.da.solution.ZaDanYaPaiSolutionCalculator;
import com.dml.paodekuai.player.action.guo.GuoAction;
import com.dml.paodekuai.preparedapai.avaliablepai.AvaliablePaiFiller;
import com.dml.paodekuai.preparedapai.fapai.FapaiStrategy;
import com.dml.paodekuai.preparedapai.lipai.ShoupaiSortStrategy;
import com.dml.paodekuai.preparedapai.luanpai.LuanpaiStrategy;
import com.dml.paodekuai.preparedapai.xianda.XiandaPlayerDeterminer;
import com.dml.paodekuai.preparedapai.zhuaniao.ZhuaniaoPlayerDeterminer;

public class Ju {

    private Pan currentPan;
    private OptionalPlay optionalPlay;

    private List<PanResult> finishedPanResultList = new ArrayList<>();
    private Map<String, String> depositPlayerList = new HashMap<>();
    private Set<String> gaungtouPlayers = new HashSet<>();
    private JuResult juResult;

    private CurrentPanFinishiDeterminer currentPanFinishiDeterminer;
    private JuFinishiDeterminer juFinishiDeterminer;

    private AvaliablePaiFiller avaliablePaiFiller;
    private LuanpaiStrategy luanpaiStrategyForFirstPan;
    private LuanpaiStrategy luanpaiStrategyForNextPan;
    private FapaiStrategy fapaiStrategyForFirstPan;
    private FapaiStrategy fapaiStrategyForNextPan;

    private ShoupaiSortStrategy shoupaiSortStrategy;
    private XiandaPlayerDeterminer xiandaPlayerDeterminer; // ?????????????????????????????????
    private ZhuaniaoPlayerDeterminer zhuaniaoPlayerDeterminer;
    private KeyaDaPaiDianShuSolutionsGenerator keyaDaPaiDianShuSolutionsGenerator;
    private YaPaiSolutionsTipsFilter yaPaiSolutionsTipsFilter;
    private AllKedaPaiSolutionsGenerator allKedaPaiSolutionsGenerator;
    private KedaPaiSolutionsForTipsGenerator kedaPaiSolutionsForTipsGenerator;

    private WaihaoGenerator waihaoGenerator;

    private ActionStatisticsListenerManager actionStatisticsListenerManager = new ActionStatisticsListenerManager();

    private CurrentPanResultBuilder currentPanResultBuilder;
    private JuResultBuilder juResultBuilder;

    private DianShuZuYaPaiSolutionCalculator dianShuZuYaPaiSolutionCalculator;
    private ZaDanYaPaiSolutionCalculator zaDanYaPaiSolutionCalculator;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void addDaListener(DaActionStatisticsListener daActionStatisticsListener) {
        actionStatisticsListenerManager.addDaListener(daActionStatisticsListener);
    }

    /**
     * ???????????????
     *
     * @param allPlayerIds ??????ID??????
     * @param startTime    ?????????
     */
    public void startFirstPan(List<String> allPlayerIds, long startTime, Map<String, Double> playerTotalScoreMap) throws Exception {
        currentPan = new Pan();
        currentPan.setNo(1);
        allPlayerIds.forEach((pid) -> currentPan.addPlayer(pid));

        // ?????????????????????????????????
        avaliablePaiFiller.fillAvaliablePai(this);
        luanpaiStrategyForFirstPan.luanpai(this);
        fapaiStrategyForFirstPan.fapai(this);
        currentPan.getPaodekuaiPlayerIdMajiangPlayerMap().values().forEach((player) -> player.lipai(shoupaiSortStrategy));

        boolean existentSan = false;
        for (PaodekuaiPlayer player : currentPan.getPaodekuaiPlayerIdMajiangPlayerMap().values()) {
            for (PukePai pukePai : player.getAllShoupai().values()) {
                if (optionalPlay.isBichu() && pukePai.getPaiMian().equals(PukePaiMian.heitaosan)) {
                    existentSan = true;
                } else if (optionalPlay.isHongxinsanxianchu() && pukePai.getPaiMian().equals(PukePaiMian.hongxinsan)) {
                    existentSan = true;
                }
            }
        }
        if (!existentSan) {
            currentPan.setNotExistentSan(true);
        }

        // ???????????????????????????
        String dapaiPlayerId = xiandaPlayerDeterminer.determineXiandaPlayerFirst(this);
        PaodekuaiPlayer player = currentPan.findPlayer(dapaiPlayerId);
        player.putYaPaiSolutionCandidates(allKedaPaiSolutionsGenerator.firstAllKedaPaiSolutions(player.getAllShoupai(), currentPan.isNotExistentSan()));

        // ??????
        player.generateNotYaPaiSolutionsForTips(yaPaiSolutionsTipsFilter);

        currentPan.updateActionPositionByActionPlayer(dapaiPlayerId);

        currentPan.addFrame(new PanActionFrame(null, new PanValueObject(currentPan), startTime));

        //????????????0????????? ???????????????????????????
        for (PaodekuaiPlayer paodekuaiPlayer : currentPan.getPaodekuaiPlayerIdMajiangPlayerMap().values()) {
            if (optionalPlay.isJinyuanzi()) {
                playerTotalScoreMap.put(paodekuaiPlayer.getId(), (double) optionalPlay.getYuanzifen());
                player.setPlayerTotalScore(optionalPlay.getYuanzifen());
            } else {
                playerTotalScoreMap.put(paodekuaiPlayer.getId(), 0d);
                player.setPlayerTotalScore(0);
            }

        }

        logger.debug(JSON.toJSONString(this));
    }

    /**
     * ???????????????
     */
    public void startNextPan() throws Exception {
        actionStatisticsListenerManager.updateListenersForNextPan();
        currentPan = new Pan();
        currentPan.setNo(countFinishedPan() + 1);
        PanResult latestFinishedPanResult = findLatestFinishedPanResult();
        List<String> allPlayerIds = latestFinishedPanResult.allPlayerIds();
        allPlayerIds.forEach((pid) -> currentPan.addPlayer(pid));

        avaliablePaiFiller.fillAvaliablePai(this);

        // ?????????????????????????????????
        luanpaiStrategyForNextPan.luanpai(this);
        fapaiStrategyForNextPan.fapai(this);
        currentPan.getPaodekuaiPlayerIdMajiangPlayerMap().values().forEach((player) -> player.lipai(shoupaiSortStrategy));

        boolean existentSan = false;
        for (PaodekuaiPlayer player : currentPan.getPaodekuaiPlayerIdMajiangPlayerMap().values()) {
            for (PukePai pukePai : player.getAllShoupai().values()) {
                if (optionalPlay.isBichu() && pukePai.getPaiMian().equals(PukePaiMian.heitaosan)) {
                    existentSan = true;
                } else if (optionalPlay.isHongxinsanxianchu() && pukePai.getPaiMian().equals(PukePaiMian.hongxinsan)) {
                    existentSan = true;
                }
            }
        }
        if (!existentSan) {
            currentPan.setNotExistentSan(true);
        }

        // ????????????????????????????????????
        String dapaiPlayerId = xiandaPlayerDeterminer.determineXiandaPlayerFirst(this);//????????????????????????3????????????
        PaodekuaiPlayer player = currentPan.findPlayer(dapaiPlayerId);
        player.putYaPaiSolutionCandidates(allKedaPaiSolutionsGenerator.firstAllKedaPaiSolutions(player.getAllShoupai(), currentPan.isNotExistentSan()));

        // ?????????????????????????????????
        player.generateNotYaPaiSolutionsForTips(yaPaiSolutionsTipsFilter);

        currentPan.updateActionPositionByActionPlayer(dapaiPlayerId);

        currentPan.addFrame(new PanActionFrame(null, new PanValueObject(currentPan), System.currentTimeMillis()));

        logger.debug(JSON.toJSONString(this));
    }

    public PanActionFrame da(String playerId, List<Integer> paiIds, String dianshuZuheIdx, long actionTime) throws Exception {
        DaAction daAction = currentPan.da(playerId, paiIds, dianshuZuheIdx, waihaoGenerator);
        // ?????????????????????
        currentPan.findPlayer(playerId).lipai(shoupaiSortStrategy);
        actionStatisticsListenerManager.updateDaActionListener(daAction, this);

        if (currentPanFinishiDeterminer.determineToFinishCurrentPan(this)) {// ???????????????
            PanResult panResult = currentPanResultBuilder.buildCurrentPanResult(this, actionTime);
            finishedPanResultList.add(panResult);
            PanActionFrame panActionFrame = currentPan.recordPanActionFrame(daAction, actionTime);
            int PlayerCount = currentPan.getPaodekuaiPlayerIdMajiangPlayerMap().size();
            currentPan = null;
            if (juFinishiDeterminer.determineToFinishJu(this) || guangtouOver(PlayerCount)) {// ???????????????
                juResult = juResultBuilder.buildJuResult(this);
            }
            return panActionFrame;
        } else {
            // ??????????????????????????????
            currentPan.updateNextPlayersDaSolution(dianShuZuYaPaiSolutionCalculator, zaDanYaPaiSolutionCalculator);
            // ??????????????????
            currentPan.generateYaPaiSolutionsForTips(yaPaiSolutionsTipsFilter);

            currentPan.updateActionPositionToNextPlayer();
            return currentPan.recordPanActionFrame(daAction, actionTime);
        }

    }

    public PanActionFrame guo(String playerId, long actionTime) throws Exception {
        GuoAction guoAction = currentPan.guo(playerId);
        // ????????????????????????????????????
        if (currentPan.ifStartYapai()) {// ???????????????????????????
            PaodekuaiPlayer nextPlayer = currentPan.findNextActionPlayer();

            boolean dachuHeotaosan = false;
            boolean dachuHongtaosan = false;
            for (PaodekuaiPlayer player : currentPan.getPaodekuaiPlayerIdMajiangPlayerMap().values()) {
                if (player.isDachuHeitaoSan()) {
                    dachuHeotaosan = true;
                } else if (player.isDachuHonxinSan()) {
                    dachuHongtaosan = true;
                }
            }

            nextPlayer.putYaPaiSolutionCandidates(allKedaPaiSolutionsGenerator.generateAllKedaPaiSolutions(nextPlayer.getAllShoupai(), currentPan.afterNextBaodan(),
                    dachuHeotaosan, dachuHongtaosan, currentPan.isNotExistentSan()));
            // ??????????????????
            nextPlayer.generateNotYaPaiSolutionsForTips(yaPaiSolutionsTipsFilter);
            PaodekuaiPlayer yapaiPlayer = null;
            try {
                yapaiPlayer = currentPan.findNextActionPlayer();
            } catch (PlayerNotFoundException e) {

            }
            if (yapaiPlayer != null) {
                yapaiPlayer.setGuo(false);
                currentPan.setActionPosition(yapaiPlayer.getPosition());
            }
            currentPan.setLatestDapaiPlayerId(null);
        } else {
            // ??????????????????????????????
            currentPan.updateNextPlayersDaSolution(dianShuZuYaPaiSolutionCalculator, zaDanYaPaiSolutionCalculator);
            // ??????????????????
            currentPan.generateYaPaiSolutionsForTips(yaPaiSolutionsTipsFilter);
            currentPan.updateActionPositionToNextPlayer();
        }
        return currentPan.recordPanActionFrame(guoAction, actionTime);
    }

    public void finish() {
        juResult = juResultBuilder.buildJuResult(this);
    }

    public int countFinishedPan() {
        return finishedPanResultList.size();
    }

    public PanResult findLatestFinishedPanResult() {
        if (!finishedPanResultList.isEmpty()) {
            return finishedPanResultList.get(finishedPanResultList.size() - 1);
        } else {
            return null;
        }
    }

    public boolean guangtouOver(int PlayerCount) {
        int guangtouPlayerCount = gaungtouPlayers.size();
        if (PlayerCount == 2 && guangtouPlayerCount >= 1) {
            return true;
        } else return guangtouPlayerCount >= 2;
    }

    /**
     * ??????????????????
     */
    public boolean canPlay(String playerId) {
        return currentPan.getPaodekuaiPlayerIdMajiangPlayerMap().get(playerId).canPlay();
    }

    /**
     * --------set/get
     */

    public Pan getCurrentPan() {
        return currentPan;
    }

    public void setCurrentPan(Pan currentPan) {
        this.currentPan = currentPan;
    }

    public CurrentPanFinishiDeterminer getCurrentPanFinishiDeterminer() {
        return currentPanFinishiDeterminer;
    }

    public void setCurrentPanFinishiDeterminer(CurrentPanFinishiDeterminer currentPanFinishiDeterminer) {
        this.currentPanFinishiDeterminer = currentPanFinishiDeterminer;
    }

    public JuFinishiDeterminer getJuFinishiDeterminer() {
        return juFinishiDeterminer;
    }

    public void setJuFinishiDeterminer(JuFinishiDeterminer juFinishiDeterminer) {
        this.juFinishiDeterminer = juFinishiDeterminer;
    }

    public AvaliablePaiFiller getAvaliablePaiFiller() {
        return avaliablePaiFiller;
    }

    public void setAvaliablePaiFiller(AvaliablePaiFiller avaliablePaiFiller) {
        this.avaliablePaiFiller = avaliablePaiFiller;
    }

    public LuanpaiStrategy getLuanpaiStrategyForFirstPan() {
        return luanpaiStrategyForFirstPan;
    }

    public void setLuanpaiStrategyForFirstPan(LuanpaiStrategy luanpaiStrategyForFirstPan) {
        this.luanpaiStrategyForFirstPan = luanpaiStrategyForFirstPan;
    }

    public FapaiStrategy getFapaiStrategyForFirstPan() {
        return fapaiStrategyForFirstPan;
    }

    public void setFapaiStrategyForFirstPan(FapaiStrategy fapaiStrategyForFirstPan) {
        this.fapaiStrategyForFirstPan = fapaiStrategyForFirstPan;
    }

    public ShoupaiSortStrategy getShoupaiSortStrategy() {
        return shoupaiSortStrategy;
    }

    public void setShoupaiSortStrategy(ShoupaiSortStrategy shoupaiSortStrategy) {
        this.shoupaiSortStrategy = shoupaiSortStrategy;
    }

    public XiandaPlayerDeterminer getXiandaPlayerDeterminer() {
        return xiandaPlayerDeterminer;
    }

    public void setXiandaPlayerDeterminer(XiandaPlayerDeterminer xiandaPlayerDeterminer) {
        this.xiandaPlayerDeterminer = xiandaPlayerDeterminer;
    }

    public ZhuaniaoPlayerDeterminer getZhuaniaoPlayerDeterminer() {
        return zhuaniaoPlayerDeterminer;
    }

    public void setZhuaniaoPlayerDeterminer(ZhuaniaoPlayerDeterminer zhuaniaoPlayerDeterminer) {
        this.zhuaniaoPlayerDeterminer = zhuaniaoPlayerDeterminer;
    }

    public KeyaDaPaiDianShuSolutionsGenerator getKeyaDaPaiDianShuSolutionsGenerator() {
        return keyaDaPaiDianShuSolutionsGenerator;
    }

    public void setKeyaDaPaiDianShuSolutionsGenerator(
            KeyaDaPaiDianShuSolutionsGenerator keyaDaPaiDianShuSolutionsGenerator) {
        this.keyaDaPaiDianShuSolutionsGenerator = keyaDaPaiDianShuSolutionsGenerator;
    }

    public YaPaiSolutionsTipsFilter getYaPaiSolutionsTipsFilter() {
        return yaPaiSolutionsTipsFilter;
    }

    public void setYaPaiSolutionsTipsFilter(YaPaiSolutionsTipsFilter yaPaiSolutionsTipsFilter) {
        this.yaPaiSolutionsTipsFilter = yaPaiSolutionsTipsFilter;
    }

    public AllKedaPaiSolutionsGenerator getAllKedaPaiSolutionsGenerator() {
        return allKedaPaiSolutionsGenerator;
    }

    public void setAllKedaPaiSolutionsGenerator(AllKedaPaiSolutionsGenerator allKedaPaiSolutionsGenerator) {
        this.allKedaPaiSolutionsGenerator = allKedaPaiSolutionsGenerator;
    }

    public KedaPaiSolutionsForTipsGenerator getKedaPaiSolutionsForTipsGenerator() {
        return kedaPaiSolutionsForTipsGenerator;
    }

    public void setKedaPaiSolutionsForTipsGenerator(KedaPaiSolutionsForTipsGenerator kedaPaiSolutionsForTipsGenerator) {
        this.kedaPaiSolutionsForTipsGenerator = kedaPaiSolutionsForTipsGenerator;
    }

    public WaihaoGenerator getWaihaoGenerator() {
        return waihaoGenerator;
    }

    public void setWaihaoGenerator(WaihaoGenerator waihaoGenerator) {
        this.waihaoGenerator = waihaoGenerator;
    }

    public ActionStatisticsListenerManager getActionStatisticsListenerManager() {
        return actionStatisticsListenerManager;
    }

    public void setActionStatisticsListenerManager(ActionStatisticsListenerManager actionStatisticsListenerManager) {
        this.actionStatisticsListenerManager = actionStatisticsListenerManager;
    }

    public CurrentPanResultBuilder getCurrentPanResultBuilder() {
        return currentPanResultBuilder;
    }

    public void setCurrentPanResultBuilder(CurrentPanResultBuilder currentPanResultBuilder) {
        this.currentPanResultBuilder = currentPanResultBuilder;
    }

    public JuResultBuilder getJuResultBuilder() {
        return juResultBuilder;
    }

    public void setJuResultBuilder(JuResultBuilder juResultBuilder) {
        this.juResultBuilder = juResultBuilder;
    }

    public List<PanResult> getFinishedPanResultList() {
        return finishedPanResultList;
    }

    public void setFinishedPanResultList(List<PanResult> finishedPanResultList) {
        this.finishedPanResultList = finishedPanResultList;
    }

    public JuResult getJuResult() {
        return juResult;
    }

    public void setJuResult(JuResult juResult) {
        this.juResult = juResult;
    }

    public DianShuZuYaPaiSolutionCalculator getDianShuZuYaPaiSolutionCalculator() {
        return dianShuZuYaPaiSolutionCalculator;
    }

    public void setDianShuZuYaPaiSolutionCalculator(DianShuZuYaPaiSolutionCalculator dianShuZuYaPaiSolutionCalculator) {
        this.dianShuZuYaPaiSolutionCalculator = dianShuZuYaPaiSolutionCalculator;
    }

    public ZaDanYaPaiSolutionCalculator getZaDanYaPaiSolutionCalculator() {
        return zaDanYaPaiSolutionCalculator;
    }

    public void setZaDanYaPaiSolutionCalculator(ZaDanYaPaiSolutionCalculator zaDanYaPaiSolutionCalculator) {
        this.zaDanYaPaiSolutionCalculator = zaDanYaPaiSolutionCalculator;
    }

    public LuanpaiStrategy getLuanpaiStrategyForNextPan() {
        return luanpaiStrategyForNextPan;
    }

    public void setLuanpaiStrategyForNextPan(LuanpaiStrategy luanpaiStrategyForNextPan) {
        this.luanpaiStrategyForNextPan = luanpaiStrategyForNextPan;
    }

    public FapaiStrategy getFapaiStrategyForNextPan() {
        return fapaiStrategyForNextPan;
    }

    public void setFapaiStrategyForNextPan(FapaiStrategy fapaiStrategyForNextPan) {
        this.fapaiStrategyForNextPan = fapaiStrategyForNextPan;
    }

    public Map<String, String> getDepositPlayerList() {
        return depositPlayerList;
    }

    public void setDepositPlayerList(Map<String, String> depositPlayerList) {
        this.depositPlayerList = depositPlayerList;
    }

    public OptionalPlay getOptionalPlay() {
        return optionalPlay;
    }

    public void setOptionalPlay(OptionalPlay optionalPlay) {
        this.optionalPlay = optionalPlay;
    }

    public Set<String> getGaungtouPlayers() {
        return gaungtouPlayers;
    }

    public void setGaungtouPlayers(Set<String> gaungtouPlayers) {
        this.gaungtouPlayers = gaungtouPlayers;
    }
}
