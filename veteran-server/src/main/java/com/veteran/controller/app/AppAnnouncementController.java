package com.veteran.controller.app;

import com.veteran.common.Result;
import com.veteran.service.AnnouncementService;
import com.veteran.vo.AnnouncementVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "小程序端-公告")
@RestController
@RequestMapping("/app/announcement")
@RequiredArgsConstructor
public class AppAnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "获取已发布的公告列表（按优先级排序）")
    @GetMapping("/active")
    public Result<List<AnnouncementVO>> getActiveList() {
        return Result.ok(announcementService.getActiveList());
    }
}
