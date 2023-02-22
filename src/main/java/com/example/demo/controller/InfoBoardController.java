package com.example.demo.controller;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.example.demo.entity.InfoBoard;
import com.example.demo.service.InfoBoardService;
import com.example.demo.service.JSONUtil;

@Controller
@RequestMapping("/goodM/infoBoard")
public class InfoBoardController {

	@Autowired private InfoBoardService infoBoardService;
	@Value("${spring.servlet.multipart.location}") private String uploadDir;
	
	@GetMapping("/list")
	public String list(HttpServletRequest req, Model model) {
		String page_ = req.getParameter("p");
		String field = req.getParameter("f");
		String query = req.getParameter("q");
		
		int page = (page_ == null || page_.equals("")) ? 1 : Integer.parseInt(page_);
		field = (field == null || field.equals("")) ? "title" : field;
		query = (query == null || query.equals("")) ? "" : query;
		List<InfoBoard> list = infoBoardService.getInfoBoardList(page, field, query);
		
		HttpSession session = req.getSession();
		session.setAttribute("currentInfoBoardPage", page);
		model.addAttribute("field", field);
		model.addAttribute("query", query);
		
		int totalInfoBoardNo = infoBoardService.getInfoBoardCount("infoBid", "");
		int totalPages = (int) Math.ceil(totalInfoBoardNo / 10.);
		
		int startPage = (int)(Math.ceil((page-0.5)/10) - 1) * 10 + 1;
		int endPage = Math.min(totalPages, startPage + 9);
		List<String> pageList = new ArrayList<>();
		for (int i = startPage; i <= endPage; i++) 
			pageList.add(String.valueOf(i));
		model.addAttribute("pageList", pageList);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("totalPages", totalPages);
		
		String today = LocalDate.now().toString(); // 2022-12-28
		model.addAttribute("today", today);
		model.addAttribute("infoBoardList", list);
		return "infoBoard/list";
	}

	@GetMapping("/detail")
	public String detail(HttpServletRequest req, Model model) {
		int infoBid = Integer.parseInt(req.getParameter("infoBid"));
		String uid = req.getParameter("uid");
		String option = req.getParameter("option");
		HttpSession session = req.getSession();
		String sessionUid = (String) session.getAttribute("uid");
		
		// 조회수 증가. 단, 본인이 읽거나 댓글 작성후에는 제외.
		if (option == null && (!uid.equals(sessionUid))) 
			infoBoardService.increaseViewCount(infoBid);
		
		InfoBoard infoBoard = infoBoardService.getInfoBoard(infoBid);
		String jsonFiles = infoBoard.getFiles();
		if (!(jsonFiles == null || jsonFiles.equals(""))) {
			JSONUtil json = new JSONUtil();
			List<String> fileList = json.parse(jsonFiles);
			model.addAttribute("fileList", fileList);
		}
		model.addAttribute("infoBoard", infoBoard);
		
		return "infoBoard/detail";
	}

	@GetMapping("/write")
	public String write() {
		return "infoBoard/write";
	}
	
	@PostMapping("/write")
	public String write(MultipartHttpServletRequest req) {
		String uid = (String) req.getParameter("uid");
		String title = (String) req.getParameter("title");
		String content = (String) req.getParameter("content");
		List<MultipartFile> fileList = req.getFiles("files");
		List<String> list = new ArrayList<>();
		// File upload
		for (MultipartFile file: fileList) {
			list.add(file.getOriginalFilename());
			String uploadFile = uploadDir + "/" + file.getOriginalFilename();
			File fileName = new File(uploadFile);
			try {
				file.transferTo(fileName);
			} catch (Exception e) {
//				e.printStackTrace();
			}
		}
		JSONUtil json = new JSONUtil();
		String files = json.stringify(list);
		InfoBoard infoBoard = new InfoBoard(uid, title, content, files); 
		infoBoardService.insertInfoBoard(infoBoard);
		return "redirect:/goodM/infoBoard/list?p=1&f=&q=";
	}
	
	@GetMapping("/update")
	public String updateForm(HttpServletRequest req, Model model) {
		int infoBid = Integer.parseInt(req.getParameter("infoBid"));
		InfoBoard infoBoard = infoBoardService.getInfoBoard(infoBid);
		HttpSession session = req.getSession();
		
		String jsonFiles = infoBoard.getFiles();
		if (!(jsonFiles == null || jsonFiles.equals(""))) {
			JSONUtil json = new JSONUtil();
			List<String> fileList = json.parse(jsonFiles);
			session.setAttribute("fileList", fileList);
		}
		model.addAttribute("infoBoard", infoBoard);
		return "infoBoard/update";
	}
	
	@PostMapping("/update")
	public String update(MultipartHttpServletRequest req) {
		int infoBid = Integer.parseInt(req.getParameter("infoBid"));
		String uid = req.getParameter("uid");
		String title = (String) req.getParameter("title");
		String content = (String) req.getParameter("content");
		
		HttpSession session = req.getSession();
		List<String> additionalFileList = (List<String>) session.getAttribute("fileList");
		String[] delFileList = req.getParameterValues("delFile");
		if (delFileList != null) {
			for (String delName: delFileList) {
				File delFile = new File(uploadDir + "/" + delName);
				delFile.delete();
				additionalFileList.remove(delName);
			}
			session.setAttribute("fileList", additionalFileList);
		}
		
		List<MultipartFile> fileList = req.getFiles("files");
		List<String> newFileList = new ArrayList<>();
		// File upload
		for (MultipartFile file: fileList) {
			newFileList.add(file.getOriginalFilename());
			String uploadFile = uploadDir + "/" + file.getOriginalFilename();
			File fileName = new File(uploadFile);
			try {
				file.transferTo(fileName);
			} catch (Exception e) {
//				e.printStackTrace();
			}
		}
		for (String fname: newFileList) {
			additionalFileList.add(fname);
		}
		List<String> newAdditionalFileList = new ArrayList<>();
		for (String afname: additionalFileList) {
			if (afname.equals(""))
				continue;
			newAdditionalFileList.add(afname);
		}
		JSONUtil json = new JSONUtil();
		String files = json.stringify(newAdditionalFileList);
		InfoBoard infoBoard = new InfoBoard(infoBid, title, content, files);
		infoBoardService.updateInfoBoard(infoBoard);
		
		return "redirect:/goodM/infoBoard/detail?infoBid=" + infoBid + "&uid=" + uid + "&option=DNI";
	}
	
	@GetMapping("/delete")
	public String delete(HttpServletRequest req, Model model) {
		int infoBid = Integer.parseInt(req.getParameter("infoBid"));
		model.addAttribute("infoBid", infoBid);
		return "infoBoard/delete";
	}
	
	@GetMapping("/deleteConfirm")
	public String deleteConfirm(HttpServletRequest req) {
		int infoBid = Integer.parseInt(req.getParameter("infoBid"));
		infoBoardService.deleteInfoBoard(infoBid);
		
		HttpSession session = req.getSession();
		return "redirect:/goodM/infoBoard/list?p=" + session.getAttribute("currentInfoBoardPage") + "&f=&q=";
	}
	
}
