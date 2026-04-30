package com.project.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.project.util.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

/**
 * 文件上传与管理控制器
 * 处理作品上传、头像上传及文件下载的相关业务
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 文件上传接口
     *
     * @param file          上传的文件对象
     * @param competitionId 竞赛ID，用于创建对应的物理存储子目录
     * @return 返回可从前端直接访问的相对 URL
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam MultipartFile file, @RequestParam Integer competitionId)
            throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null)
            return Result.error("文件名称不能为空");

        // 提取纯文件名，防止路径穿越（如 ../../etc/passwd）
        originalFilename = new File(originalFilename).getName();

        String dirPath = uploadDir + competitionId + "/";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File saveFile = new File(dirPath + originalFilename);
        file.transferTo(saveFile);

        String url = "/" + competitionId + "/" + originalFilename;
        return Result.success(url);
    }

    /**
     * 头像上传接口
     * 使用 UUID 重命名，避免文件名冲突
     *
     * @param file 上传的头像文件
     * @return 返回可从前端直接访问的相对 URL
     */
    @PostMapping("/uploadHead")
    public Result<String> uploadHead(@RequestParam MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null)
            return Result.error("文件名称不能为空");

        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = cn.hutool.core.util.IdUtil.fastSimpleUUID() + ext;

        String dirPath = uploadDir + "head/";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File saveFile = new File(dirPath + fileName);
        file.transferTo(saveFile);

        String url = "/head/" + fileName;
        return Result.success(url);
    }

    /**
     * 文件下载/查看接口
     *
     * @param flag     文件名（精确匹配）
     * @param response HTTP 响应对象
     */
    @GetMapping("/{flag}")
    public void getFiles(@PathVariable String flag, HttpServletResponse response) {
        try {
            List<String> fileNames = FileUtil.listFileNames(uploadDir);

            // 精确匹配文件名，避免 contains 模糊匹配导致的不确定性
            String fileName = fileNames.stream()
                    .filter(name -> name.equals(flag))
                    .findFirst()
                    .orElse("");

            if (StrUtil.isEmpty(fileName)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            byte[] bytes = FileUtil.readBytes(uploadDir + fileName);
            response.addHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            response.setContentType("application/octet-stream");
            OutputStream os = response.getOutputStream();
            os.write(bytes);
            os.flush();
        } catch (Exception e) {
            log.error("文件下载处理异常", e);
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (Exception ignored) {
            }
        }
    }
}
