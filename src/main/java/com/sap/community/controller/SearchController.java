package com.sap.community.controller;

import com.sap.community.entity.DiscussPost;
import com.sap.community.entity.Page;
import com.sap.community.service.ElasticSearchService;
import com.sap.community.service.LikeService;
import com.sap.community.service.UserService;
import com.sap.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticSearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) throws IOException {

        page.setRows((int) elasticsearchService.findDiscussPostCount(keyword));
        page.setPath("/search?keyword=" + keyword);

        //搜索帖子
        List<DiscussPost> discussPostList = elasticsearchService.findDiscussPost(keyword, page.getOffset(), page.getLimit());

        //聚合数据
        List<Map<String,Object>> discussPostViewList = new ArrayList<>();
        if (discussPostList != null) {
            for (DiscussPost post : discussPostList) {
                Map<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", post);
                // 作者
                map.put("user", userService.findUserById(post.getUserId()));
                // 点赞数量
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));

                discussPostViewList.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPostViewList);
        model.addAttribute("keyword", keyword);

        return "/site/search";

    }

}
