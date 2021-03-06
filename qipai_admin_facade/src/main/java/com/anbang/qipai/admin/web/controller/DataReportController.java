package com.anbang.qipai.admin.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.anbang.qipai.admin.constant.Constants;
import com.anbang.qipai.admin.plan.bean.chaguan.ChaguanGameDataReport;
import com.anbang.qipai.admin.plan.bean.games.Game;
import com.anbang.qipai.admin.plan.bean.members.CardSouceEnum;
import com.anbang.qipai.admin.plan.bean.members.MemberDbo;
import com.anbang.qipai.admin.plan.bean.members.MemberType;
import com.anbang.qipai.admin.plan.bean.report.BasicDataReport;
import com.anbang.qipai.admin.plan.bean.report.DetailedReport;
import com.anbang.qipai.admin.plan.bean.report.GameDataReport;
import com.anbang.qipai.admin.plan.bean.report.OnlineStateRecord;
import com.anbang.qipai.admin.plan.bean.report.PlatformReport;
import com.anbang.qipai.admin.plan.service.GameReportService;
import com.anbang.qipai.admin.plan.service.PlatformReportService;
import com.anbang.qipai.admin.plan.service.membersservice.MemberDboService;
import com.anbang.qipai.admin.plan.service.membersservice.MemberLoginRecordService;
import com.anbang.qipai.admin.plan.service.membersservice.MemberOrderService;
import com.anbang.qipai.admin.plan.service.membersservice.MemberTypeService;
import com.anbang.qipai.admin.plan.service.reportservice.BasicDataReportService;
import com.anbang.qipai.admin.plan.service.reportservice.DetailedReportService;
import com.anbang.qipai.admin.plan.service.reportservice.OnlineStateRecordService;
import com.anbang.qipai.admin.util.CalculateUtils;
import com.anbang.qipai.admin.util.CommonVOUtil;
import com.anbang.qipai.admin.util.FormatUtils;
import com.anbang.qipai.admin.util.TimeUtil;
import com.anbang.qipai.admin.web.query.MemberQuery;
import com.anbang.qipai.admin.web.vo.CommonVO;
import com.anbang.qipai.admin.web.vo.membersvo.MemberOrderVO;
import com.anbang.qipai.admin.web.vo.reportvo.AddUserCountVO;
import com.anbang.qipai.admin.web.vo.reportvo.CommonRatioVo;
import com.anbang.qipai.admin.web.vo.reportvo.CurrentCountVO;
import com.anbang.qipai.admin.web.vo.reportvo.GraphVO;
import com.anbang.qipai.admin.web.vo.reportvo.SubtotalVO;
import com.highto.framework.web.page.ListPage;

/**
 * ????????????controller
 *
 * @author ????????? 2018.7.9
 *
 */
@CrossOrigin
@RestController
@RequestMapping("/datareport")
public class DataReportController {
	private final int CURRENT_TIME_COUNT = 0;
	private final int EVERY_DAY_COUNT = 1;
	private final int EVERY_WEEK_COUNT = 2;
	private final int EVERY_MONTH_COUNT = 3;

	@Autowired
	private PlatformReportService platformReportService;

	@Autowired
	private GameReportService gameReportService;

	@Autowired
	private MemberDboService memberService;

	@Autowired
	private MemberOrderService orderService;

	@Autowired
	private MemberLoginRecordService memberLoginRecordService;

	@Autowired
	private BasicDataReportService basicDataReportService;

	@Autowired
	private OnlineStateRecordService onlineStateRecordService;

	@Autowired
	private DetailedReportService detailedReportService;

	@Autowired
	private MemberTypeService memberTypeService;

	/**
	 * ??????????????????
	 */
	@RequestMapping(value = "/platformreport", method = RequestMethod.POST)
	public CommonVO platformOperateReport(@RequestParam(required = true) Long startTime,
			@RequestParam(required = true) Long endTime, @RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer size) {
		CommonVO vo = new CommonVO();
		ListPage listPage = platformReportService.findPlatformReportByTime(page, size, startTime, endTime);
		vo.setSuccess(true);
		vo.setMsg("platformReportList");
		vo.setData(listPage);
		return vo;
	}

	/**
	 * ??????????????????????????????
	 */
	@RequestMapping(value = "/gamereport", method = RequestMethod.POST)
	public CommonVO gameReport(@RequestParam(required = true) Long startTime,
			@RequestParam(required = true) Long endTime, @RequestParam(required = true) Game game) {
		CommonVO vo = new CommonVO();
		List<GameDataReport> reportList = gameReportService.findGameReportByTimeAndGame(startTime, endTime, game);
		vo.setSuccess(true);
		vo.setMsg("gameDataList");
		vo.setData(reportList);
		return vo;
	}

	/**
	 * ??????????????????
	 */
	@RequestMapping(value = "/chaguanreport", method = RequestMethod.POST)
	public CommonVO chaguanReport(@RequestParam(required = true) Long startTime,
			@RequestParam(required = true) Long endTime, @RequestParam(required = true) Game game) {
		CommonVO vo = new CommonVO();
		List<ChaguanGameDataReport> reportList = gameReportService.findChaguanGameDataReportByTimeAndGame(startTime,
				endTime, game);
		vo.setSuccess(true);
		vo.setMsg("gameDataList");
		vo.setData(reportList);
		return vo;
	}

	/**
	 * ????????????????????????
	 */
	@Scheduled(cron = "0 0 2 * * ?") // ????????????2???
	public void createPlatformReport() {
		long oneDay = 3600000 * 24;
		// ????????????2???
		long endTime = System.currentTimeMillis();
		// ????????????2???
		long startTime = endTime - oneDay;
		int newMember = (int) memberService.countNewMemberByTime(startTime, endTime);
		int currentMember = (int) memberService.countVipMember();
		double cost = orderService.countCostByTime(startTime, endTime);
		int gameNum = gameReportService.countGameNumByTime(startTime, endTime);

		int loginMember = memberLoginRecordService.countLoginMemberByTime(startTime, endTime);

		int remainSecond = memberLoginRecordService.countRemainMemberByDeviationTime(oneDay);
		int remainThird = memberLoginRecordService.countRemainMemberByDeviationTime(oneDay * 2);
		int remainSeventh = memberLoginRecordService.countRemainMemberByDeviationTime(oneDay * 6);
		int remainMonth = memberLoginRecordService.countRemainMemberByDeviationTime(oneDay * 30);
		PlatformReport report = new PlatformReport(endTime, newMember, currentMember, cost, gameNum, loginMember,
				remainSecond, remainThird, remainSeventh, remainMonth);
		platformReportService.addPlatformReport(report);
	}

	/**
	 * ??????????????????(???????????????)
	 * 
	 * @param currentTime
	 * @return
	 */
	@PostMapping(value = "/addUserCount")
	public CommonVO addUserCount(Long currentTime) {
		// 1.????????????????????????????????????????????????,??????????????????
		Long startTime = TimeUtil.getBeginDayTimeOfCurrentMonth(currentTime);
		Long endTime = TimeUtil.getEndDayTimeOfCurrentMonth(currentTime);
		// 2.??????????????????????????????(???????????????)
		List<PlatformReport> reportList = platformReportService.findAllPlatformReportByTime(startTime, endTime);

		// 3.???????????????????????????????????????????????????????????????
		int totalMember = (int) memberService.countNewMemberByTime(1535731200000L, startTime);

		// 4.??????List(?????????,count)
		List<AddUserCountVO> addUserCountList = new ArrayList<>();
		for (PlatformReport platformReport : reportList) {
			// ??????????????????0?????????
			if (platformReport.getNewMember() != 0) {
				AddUserCountVO addUserCount = new AddUserCountVO();
				addUserCount.setDate(platformReport.getDate());
				addUserCount.setNewMember(platformReport.getNewMember());
				// ????????????????????????
				totalMember += platformReport.getNewMember();
				addUserCount.setTotalMember(totalMember);
				addUserCountList.add(addUserCount);
			}
		}

		return CommonVOUtil.success(addUserCountList, "addUserCountList");
	}

	/**
	 * ?????????????????????
	 * 
	 * @return
	 */
	@PostMapping(value = "/addUserGraph")
	public CommonVO addUserGraph() {
		// ???????????????????????????,????????????,????????????
		// ??????????????????(?????????)
		int[] addUserToday = new int[24];
		long dayStartTime = TimeUtil.getDayStartTime(new Date());
		for (MemberDbo memberDbo : findMemberAfterTime(dayStartTime)) {
			// ????????????memberdbo???????????????
			int clock = TimeUtil.getClockByTime(memberDbo.getCreateTime());
			// ???????????????
			addUserToday[clock]++;
		}

		// ????????????????????????(?????????)
		int[] addUserWeek = new int[7];
		long weekStartTime = TimeUtil.getWeekStartTime();
		for (MemberDbo memberDbo : findMemberAfterTime(weekStartTime)) {
			// ????????????memberdbo?????????????????????(0-6)
			int weekTime = TimeUtil.getWeekByTime(memberDbo.getCreateTime()) - 1;
			// ???????????????
			addUserWeek[weekTime]++;
		}

		// ??????????????????(?????????)
		int[] addUserMonth = new int[31];
		long monthStartTime = TimeUtil.getBeginDayTimeOfCurrentMonth(System.currentTimeMillis());
		for (MemberDbo memberDbo : findMemberAfterTime(monthStartTime)) {
			// ????????????memberdbo???????????????(0-31),????????????29???30???
			int monthTime = TimeUtil.getMonthByTime(memberDbo.getCreateTime()) - 1;
			addUserMonth[monthTime]++;
		}
		// ????????????????????????,????????????
		int days = TimeUtil.getDaysByTime(System.currentTimeMillis());
		int[] ActualAddUserMonth = Arrays.copyOf(addUserMonth, days);

		GraphVO graphVO = new GraphVO(addUserToday, addUserWeek, ActualAddUserMonth);
		return CommonVOUtil.success(graphVO, "??????");
	}

	private List<MemberDbo> findMemberAfterTime(long startTime) {
		return memberService.findMemberAfterTime(startTime);
	}

	/**
	 * ?????????????????????
	 * 
	 * @return
	 */
	@PostMapping(value = "/onlineUserGraph")
	public CommonVO onlineUserGraph() {

		int[] countByToday = new int[24];
		long dayStartTime = TimeUtil.getDayStartTime(new Date());
		for (BasicDataReport dataReport : findBasicDataAfterTime(dayStartTime)) {
			int clock = TimeUtil.getClockByTime(dataReport.getCreateTime());
			countByToday[clock] = dataReport.getMaxQuantity();
		}

		int[] countByWeek = new int[7];
		long weekStartTime = TimeUtil.getWeekStartTime();
		for (BasicDataReport dataReport : findBasicDataAfterTime(weekStartTime)) {
			// ?????????????????????(0-6)
			int weekTime = TimeUtil.getWeekByTime(dataReport.getCreateTime()) - 1;
			if (dataReport.getMaxQuantity() > countByWeek[weekTime]) {
				// ??????????????????????????????????????????
				countByWeek[weekTime] = dataReport.getMaxQuantity();
			}
		}

		int[] countByMonth = new int[31];
		long monthStartTime = TimeUtil.getBeginDayTimeOfCurrentMonth(System.currentTimeMillis());
		for (BasicDataReport dataReport : findBasicDataAfterTime(monthStartTime)) {
			// ?????????????????????????????????(0-31),????????????29???30???
			int monthTime = TimeUtil.getMonthByTime(dataReport.getCreateTime()) - 1;
			if (dataReport.getMaxQuantity() > countByMonth[monthTime]) {
				// ??????????????????????????????????????????
				countByMonth[monthTime] = dataReport.getMaxQuantity();
			}
		}

		// ????????????????????????,????????????
		int days = TimeUtil.getDaysByTime(System.currentTimeMillis());
		int[] ActualAddUserMonth = Arrays.copyOf(countByMonth, days);
		GraphVO graphVO = new GraphVO(countByToday, countByWeek, ActualAddUserMonth);
		return CommonVOUtil.success(graphVO, "??????");
	}

	private List<BasicDataReport> findBasicDataAfterTime(long startTime) {
		return basicDataReportService.findBasicDataAfterTime(startTime);
	}

	/**
	 * ?????????????????????
	 * 
	 * @return
	 */
	@PostMapping(value = "/powerCountGraph")
	public CommonVO powerCountGraph() {

		// ??????????????????:??????????????????
		int[] countByToday = new int[24];
		long dayStartTime = TimeUtil.getDayStartTime(new Date());
		for (OnlineStateRecord stateRecord : onlineStateRecordService.findOnlineRecordAfterTime(dayStartTime)) {
			int clock = TimeUtil.getClockByTime(stateRecord.getCreateTime());
			countByToday[clock]++;
		}

		// ????????????,??????????????????:?????????
		int[] countByWeek = new int[7];
		long weekStartTime = TimeUtil.getWeekStartTime();
		for (DetailedReport detailedReport : findDetailedReportAfterTime(weekStartTime)) {
			// ?????????????????????(0-6)
			int weekTime = TimeUtil.getWeekByTime(detailedReport.getCreateTime()) - 1;
			countByWeek[weekTime] = detailedReport.getPowerCount();
		}

		int[] countByMonth = new int[31];
		long monthStartTime = TimeUtil.getBeginDayTimeOfCurrentMonth(System.currentTimeMillis());
		for (DetailedReport detailedReport : findDetailedReportAfterTime(monthStartTime)) {
			// ?????????????????????????????????(0-31),????????????29???30???
			int monthTime = TimeUtil.getMonthByTime(detailedReport.getCreateTime()) - 1;
			countByMonth[monthTime] = detailedReport.getPowerCount();
		}

		// ????????????????????????,????????????
		int days = TimeUtil.getDaysByTime(System.currentTimeMillis());
		int[] ActualAddUserMonth = Arrays.copyOf(countByMonth, days);
		GraphVO graphVO = new GraphVO(countByToday, countByWeek, ActualAddUserMonth);
		return CommonVOUtil.success(graphVO, "??????");
	}

	/**
	 * ?????????????????????
	 * 
	 * @return
	 */
	@PostMapping(value = "/activeUserGraph")
	public CommonVO activeUserGraph() {
		// ????????????,??????????????????:?????????
		int[] countByWeek = new int[7];
		long weekStartTime = TimeUtil.getWeekStartTime();
		for (DetailedReport detailedReport : findDetailedReportAfterTime(weekStartTime)) {
			// ?????????????????????(0-6)
			int weekTime = TimeUtil.getWeekByTime(detailedReport.getCreateTime()) - 1;
			countByWeek[weekTime] = detailedReport.getActiveUser();
		}

		int[] countByMonth = new int[31];
		long monthStartTime = TimeUtil.getBeginDayTimeOfCurrentMonth(System.currentTimeMillis());
		for (DetailedReport detailedReport : findDetailedReportAfterTime(monthStartTime)) {
			// ?????????????????????????????????(0-31),????????????29???30???
			int monthTime = TimeUtil.getMonthByTime(detailedReport.getCreateTime()) - 1;
			countByMonth[monthTime] = detailedReport.getActiveUser();
		}

		// ????????????????????????,????????????
		int days = TimeUtil.getDaysByTime(System.currentTimeMillis());
		int[] ActualAddUserMonth = Arrays.copyOf(countByMonth, days);
		GraphVO graphVO = new GraphVO(countByWeek, ActualAddUserMonth);
		return CommonVOUtil.success(graphVO, "??????");
	}

	/**
	 * ??????????????????(????????????,??????????????????,?????????????????????)
	 * 
	 * @return
	 */
	@PostMapping(value = "/activeUserSubtotal")
	public CommonVO activeUserSubtotal() {
		// ??????????????????
		DetailedReport detailedReport = detailedReportService.findByCreateTime(TimeUtil.getTimeWithLastDay());
		SubtotalVO lastDay = new SubtotalVO(detailedReport.getActiveUser(), detailedReport.getDayOnlineTime());
		// ????????????????????????
		// ???????????????????????????
		List<DetailedReport> sevenDayList = findDetailedReportAfterTime(TimeUtil.getTimeWithLastSevenDay());
		SubtotalVO sevenDay = getSubtotalVO(sevenDayList);

		// ???????????????????????????
		// ??????????????????????????????
		List<DetailedReport> thirtyDayList = findDetailedReportAfterTime(TimeUtil.getTimeWithLastThirtyDay());
		SubtotalVO thirtyDay = getSubtotalVO(thirtyDayList);

		// ????????????
		List<SubtotalVO> subtotalVOList = new ArrayList<>();
		subtotalVOList.add(lastDay);
		subtotalVOList.add(sevenDay);
		subtotalVOList.add(thirtyDay);

		// ????????????subtotalVO?????????(????????????)
		return CommonVOUtil.success(subtotalVOList, "??????");
	}

	private List<DetailedReport> findDetailedReportAfterTime(long startTime) {
		return detailedReportService.findDetailedReportAfterTime(startTime);
	}

	private SubtotalVO getSubtotalVO(List<DetailedReport> list) {
		int activeUser = 0;
		long dayOnlineTime = 0L;
		for (DetailedReport report : list) {
			activeUser += report.getActiveUser();
			dayOnlineTime += report.getDayOnlineTime();
		}
		return new SubtotalVO(activeUser / list.size(), dayOnlineTime / list.size());
	}

	/**
	 * ???????????????????????????
	 * 
	 * @return
	 */
	@PostMapping(value = "/currentCount")
	public CommonVO currentCount() {
		// ????????????
		Integer onlineCount = memberService.countOnlineState();
		// ????????????
		Integer activeUserCount = detailedReportService.findByCreateTime(TimeUtil.getTimeWithLastDay()).getActiveUser();

		// ???????????????????????????
		DetailedReport report = detailedReportService
				.findByCreateTime(TimeUtil.getTimeWithDayPrecision(System.currentTimeMillis()));
		// ???????????????????????????????????????(?????????????????????)
		if (report == null) {
			// ??????????????????????????????
			Integer lastlaunchCount = detailedReportService.findByCreateTime(TimeUtil.getTimeWithLastDay())
					.getPowerCount();
			return CommonVOUtil.success(new CurrentCountVO(0, onlineCount, lastlaunchCount, activeUserCount),
					"currentCount");
		}
		// ????????????
		Integer addCountToday = report.getAddUserCount();
		// ????????????
		Integer launchCount = report.getPowerCount();
		return CommonVOUtil.success(new CurrentCountVO(addCountToday, onlineCount, launchCount, activeUserCount),
				"currentCount");
	}

	private Integer inspect(Object obj) {
		if (obj == null) {
			return 0;
		}
		return (Integer) obj;
	}

	/**
	 * ???????????????
	 * 
	 * @param currentTime
	 * @return
	 */
	@PostMapping(value = "/getDetailedReport")
	public CommonVO getDetailedReport(Long currentTime) {
		// ???????????????????????????????????????
		Long startTime = TimeUtil.getBeginDayTimeOfCurrentMonth(currentTime);
		Long endTime = TimeUtil.getEndDayTimeOfCurrentMonth(currentTime);
		// ??????DetailedReport???,???????????????????????????,?????????list???
		List<DetailedReport> reportList = detailedReportService.findByTime(startTime, endTime);
		return CommonVOUtil.success(reportList, "DetailedReport");
	}

	/**
	 * ??????????????????
	 */
	@PostMapping(value = "/silencePlayer")
	public CommonVO silencePlayer(@RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer size, String playerId, String nickName, String onlineState) {
		MemberType memberType = new MemberType();
		memberType.setCardSource(CardSouceEnum.PLAYER);
		long time = System.currentTimeMillis() - Constants.SEVEN_DAY_MS;
		// ??????????????????????????????7????????????ids
		List<String> ids = memberTypeService.listIdsByBeanAndTime(memberType, time);

		MemberQuery query = new MemberQuery();
		query.setId(playerId);
		query.setNickname(nickName);
		query.setOnlineState(onlineState);
		query.setIds(ids);
		ListPage listPage = memberService.findMemberDboByQuery(page, size, query);
		long totalSilence = memberService.countMemberDboByQuery(ids);

		Map data = new HashMap();
		data.put("listPage", listPage);
		data.put("totalSilence", totalSilence);
		return CommonVOUtil.success(data, "silencePlayer");
	}

	/**
	 * ??????????????????(?????????)
	 */
	@PostMapping(value = "/paidStatistics")
	public CommonVO paidStatistics() {
		Map data = new HashMap();

		// ??????????????????????????????
		List<CommonRatioVo> nowPie = memberTypeService.queryRatio();
		data.put("nowPie", nowPie);

		// ????????????????????????
		long startTime = TimeUtil.getTimeWithLastDay();
		Long endTime = TimeUtil.getEndTimeWithLastDay();
		List<String> timeRangeIds = onlineStateRecordService.listIdsByTime(startTime, endTime);
		List<CommonRatioVo> yesterdayPie = memberTypeService.queryRatio(timeRangeIds);
		data.put("yesterdayPie", yesterdayPie);

		// ??????????????????
		List<CommonRatioVo> payPie = new ArrayList<>();
		List<String> ids = memberTypeService.listIdsByBeanAndTime(new MemberType(), null);
		int playerCount = (int) memberService.countAmount();
		double payPlayerRatio = CalculateUtils.div(ids.size(), playerCount, 4);
		payPie.add(new CommonRatioVo("????????????", ids.size(), payPlayerRatio));
		payPie.add(new CommonRatioVo("???????????????", playerCount, 1 - payPlayerRatio));
		data.put("payPie", payPie);

		return CommonVOUtil.success(data, "paidStatistics");
	}

	/**
	 * ???????????????
	 */
	@PostMapping(value = "/sellCards")
	public CommonVO sellCards(Integer yearMonth) {

		Map<String, Object> data = new HashMap<String, Object>();
		if (yearMonth == null) {
			return CommonVOUtil.error("Missing required input parameters");
		}

		if (!FormatUtils.monthCheck(yearMonth)) {
			return CommonVOUtil.error("Incorrect parameter format");
		}

		// ????????????
		MemberOrderVO memberOrderVO = new MemberOrderVO();
		memberOrderVO.setStatus("PAYSUCCESS");
		memberOrderVO.setOrderMonth(yearMonth);
		memberOrderVO.setProductName("??????");
		double memberRiNum = orderService.sumField(memberOrderVO, "number");
		double memberRiAmount = orderService.sumField(memberOrderVO, "totalamount");
		memberOrderVO.setProductName("??????");
		double memberZhouNum = orderService.sumField(memberOrderVO, "number");
		double memberZhouAmount = orderService.sumField(memberOrderVO, "totalamount");
		memberOrderVO.setProductName("??????");
		double memberYueNum = orderService.sumField(memberOrderVO, "number");
		double memberYueAmount = orderService.sumField(memberOrderVO, "totalamount");
		memberOrderVO.setProductName("??????");
		double memberJiNum = orderService.sumField(memberOrderVO, "number");
		double memberJiAmount = orderService.sumField(memberOrderVO, "totalamount");
		data.put("memberRiNum", memberRiNum);
		data.put("memberRiAmount", memberRiAmount);
		data.put("memberZhouNum", memberZhouNum);
		data.put("memberZhouAmount", memberZhouAmount);
		data.put("memberYueNum", memberYueNum);
		data.put("memberYueAmount", memberYueAmount);
		data.put("memberJiNum", memberJiNum);
		data.put("memberJiAmount", memberJiAmount);

		// ?????????????????????
		int month = Integer.valueOf(yearMonth);
		MemberOrderVO orderVO = new MemberOrderVO();
		orderVO.setStatus("PAYSUCCESS");
		orderVO.setOrderMonth(month);
		double memberBuy = orderService.sumField(orderVO, "totalamount");
		data.put("memberBuy", memberBuy);

		// ???arpu
		double arpu = 0;
		int monthPayPlayer = orderService.countMonthPayPlayer(yearMonth);
		if (monthPayPlayer != 0) {
			arpu = CalculateUtils.div(memberBuy, (double) monthPayPlayer, 2);
		}
		data.put("arpu", arpu);

		return CommonVOUtil.success(data, "sellCards");
	}

}
