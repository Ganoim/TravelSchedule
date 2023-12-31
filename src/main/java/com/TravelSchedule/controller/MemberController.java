package com.TravelSchedule.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.TravelSchedule.dto.Calendar;
import com.TravelSchedule.dto.Member;
import com.TravelSchedule.service.MemberService;

@Controller
public class MemberController {

	@Autowired
	MemberService msvc;

	@RequestMapping(value = "/memberLogout")
	public ModelAndView memberLogout(HttpSession session) {
		System.out.println("로그아웃 요청");
		ModelAndView mav = new ModelAndView();
		session.removeAttribute("loginId");
		session.removeAttribute("loginProfile");
		session.removeAttribute("loginNickname");
		session.removeAttribute("loginState");
		mav.setViewName("redirect:/");
		return mav;
	}

	@RequestMapping(value = "/memberLogin")
	public ModelAndView memberLogin(Member mem, HttpSession session, RedirectAttributes ra) {
		System.out.println("로그인 요청");
		ModelAndView mav = new ModelAndView();
		Member rs = msvc.memberLogin(mem);
		if (rs == null) {
			mav.setViewName("redirect:/memberLoginForm");
		} else {
			session.setAttribute("loginState", rs.getMstate());
			String mstate = (String) session.getAttribute("loginState");
			System.out.println("mstate:"+mstate);
			if (mstate.equals("AD")) {
				System.out.println("관리자로그인");
				session.setAttribute("loginId", rs.getMid());
				session.setAttribute("loginProfile", rs.getMprofile());
				session.setAttribute("loginNickname", rs.getMnickname());
				session.setAttribute("loginState", rs.getMstate());
				mav.setViewName("/admin/adminMain");
			} else if (mstate.equals("NN")) {
				System.out.println("이용이 정지된 계정입니다.");
				ra.addFlashAttribute("msg", "이용이 정지된 계정입니다. 관리자에게 문의해주세요!");
				mav.setViewName("redirect:/memberLoginForm");
			} else {
				session.setAttribute("loginId", rs.getMid());
				session.setAttribute("loginProfile", rs.getMprofile());
				session.setAttribute("loginNickname", rs.getMnickname());
				session.setAttribute("loginState", rs.getMstate());
				mav.setViewName("redirect:/");
			}
		}
		return mav;
	}

	@RequestMapping(value = "/memberJoin")
	public ModelAndView memberJoin(Member meminfo, HttpSession session) {
		System.out.println("회원가입 요청");
		ModelAndView mav = new ModelAndView();
		Member mem = msvc.setMprofile(meminfo, session);
		System.out.println(mem);
		int rs = msvc.memberJoin(meminfo);
		if (rs > 0) {
			mav.setViewName("redirect:/");
		} else {
			mav.setViewName("redirect:/memberJoinForm");
		}
		return mav;
	}

	@RequestMapping(value = "/checkInfo")
	public @ResponseBody String idCheck(String info, String location) {
		System.out.println("아이디/닉네임 중복체크 요청");
		String rs = msvc.checkinfo(info, location);
		System.out.println("rs" + rs);
		return rs;
	}

	@RequestMapping(value = "/myInfo")
	public ModelAndView myInfo(HttpSession session) {
		System.out.println("내정보 조회 요청");
		ModelAndView mav = new ModelAndView();

		String loginId = (String) session.getAttribute("loginId");
		System.out.println("조회 할 아이디 : " + loginId);

		Member member = msvc.getMemberInfo(loginId);
		System.out.println(member);

		mav.addObject("mInfo", member);
		mav.setViewName("member/myInfo");
		return mav;
	}

	@RequestMapping(value = "/memberUpdate")
	public ModelAndView memberUpdate(Member mem, HttpSession session, RedirectAttributes ra) {
		System.out.println("회원정보 수정 요청");
		ModelAndView mav = new ModelAndView();
		
		int memInfo = msvc.memberInfo(mem, session);

		System.out.println(memInfo);

		if (memInfo > 0) {
			mav.setViewName("redirect:/myInfo");
			ra.addFlashAttribute("msg", "회원정보가 수정되었습니다.");
		} else {
			mav.setViewName("redirect:/myInfo");
			ra.addFlashAttribute("msg", "회원정보 수정에 실패하였습니다.");
		}

		return mav;
	}

	@RequestMapping(value = "/updatePw")
	public ModelAndView updatePw(Member mem, RedirectAttributes ra) {
		System.out.println("회원 비밀번호 수정 요청");
		ModelAndView mav = new ModelAndView();
		System.out.println(mem);
		
		int memInfo = msvc.newPassword(mem);
		
		if (memInfo > 0) {
			mav.setViewName("redirect:/myInfo");
			ra.addFlashAttribute("msg", "비밀번호가 변경되었습니다.");
		} else {
			mav.setViewName("redirect:/myInfo");
			ra.addFlashAttribute("msg", "비밀번호 변경에 실패하였습니다.");
		}

		return mav;
	}

	@RequestMapping(value = "memberLogin_Kakao")
	public @ResponseBody String memberLogin_Kakao(String id, HttpSession session) {
		System.out.println("카카오 로그인 요청");
		System.out.println("받아온 id : " + id);

		Member loginMember = msvc.LoginMemberInfo_kakao(id);

		if (loginMember == null) {
			System.out.println("카카오 계정 정보 없음");
			return "N";
		} else {
			System.out.println("카카오 계정 정보 있음");
			System.out.println("로그인 처리");
			session.setAttribute("loginId", loginMember.getMid());
			session.setAttribute("loginProfile", loginMember.getMprofile());
			session.setAttribute("loginNickname", loginMember.getMnickname());
			session.setAttribute("loginState", loginMember.getMstate());
			return "Y";
		}
	}

	@RequestMapping(value = "/memberJoin_Kakao")
	public @ResponseBody String memberJoin_kakao(Member member) {
		System.out.println("카카오 계정 회원가입요청");
		System.out.println(member);

		int result = msvc.registMember_kakao(member);

		return result + "";
	}

	@RequestMapping(value = "NaverLoginCheck")
	public @ResponseBody String NaverLoginCheck(String id, HttpSession session) {
		System.out.println("네이버 로그인 아이디 유무 확인");
		System.out.println("받아온 id : " + id);
		String mid = id.substring(0, 20);
		System.out.println("번환 후 :" + mid);
		Member NaverLog = msvc.CheckNaverLog(mid);
		if (NaverLog == null) {
			System.out.println("네이버 계정 정보 없음");
			return "N";
		} else {
			System.out.println("로그인 정보 있음");
			session.setAttribute("loginId", NaverLog.getMid());
			session.setAttribute("loginNickname", NaverLog.getMnickname());
			session.setAttribute("loginProfile", NaverLog.getMprofile());
			session.setAttribute("loginState", NaverLog.getMstate());
			return "Y";
		}
	}

	@RequestMapping(value = "MemberJoin_Naver")
	public @ResponseBody String MemberJoin_Naver(Member member) {
		System.out.println("네이버 회원가입 요청");
		System.out.println(member);
		String mid = member.getMid().substring(0, 20);
		member.setMid(mid);
		int result = msvc.registMember_Naver(member);

		return result + "";
	}

	@RequestMapping(value = "/memberList")
	public ModelAndView memberList(Member member) {
		System.out.println("회원관리페이지 이동");
		ModelAndView mav = new ModelAndView();

		ArrayList<Member> mList = msvc.getMemberList(member);
		mav.addObject("mList", mList);
		mav.setViewName("/admin/memberList");

		return mav;
	}

	@RequestMapping(value = "/mstateNN")
	public @ResponseBody String mstateNN(String mid) {
		System.out.println("회원정지");
		System.out.println("정지할 회원아이디 : " + mid);
		int memberState = msvc.memState(mid);
		if (0 < memberState) {
			System.out.println("정지성공");
			return "Y";
		} else {
			System.out.println("정지실패");
			return "N";
		}

	}

	@RequestMapping(value = "/mstateNY")
	public @ResponseBody String mstateNY(String mid, String mpw) {
		System.out.println("회원정지해제");
		int memberStateY = msvc.memStateY(mid, mpw);
		if (0 < memberStateY) {
			System.out.println("정지해제성공");
			return "Y";
		} else {
			System.out.println("정지해제실패");
			return "N";
		}

	}
}
