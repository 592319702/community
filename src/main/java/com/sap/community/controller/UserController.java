package com.sap.community.controller;

import com.sap.community.annotation.LoginRequired;
import com.sap.community.entity.User;
import com.sap.community.service.FollowService;
import com.sap.community.service.LikeService;
import com.sap.community.service.UserService;
import com.sap.community.util.CommunityConstant;
import com.sap.community.util.CommunityUtil;
import com.sap.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if(headerImage == null) {
            model.addAttribute("error", "there's no image");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "unformat image");
            return "/site/setting";
        }
        // ?????????????????????
        fileName = CommunityUtil.generateUUID() + suffix;
        // ???????????????????????????
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // ????????????
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("File to upload image" + e.getMessage());
            throw new RuntimeException(e);
        }
        // ????????????????????????????????????(web????????????)
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // ?????????????????????
        fileName = uploadPath + "/" + fileName;
        // ????????????
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // ????????????
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("load image fail: " + e.getMessage());
        }
    }

    // ????????????
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("user not exist");
        }
        // user
        model.addAttribute("user", user);
        // like
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        // follow
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        // follower
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);
        // isfollow
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "/site/profile";
    }

}
