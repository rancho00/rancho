package com.rancho.web.admin.controller;

import com.rancho.web.admin.annotation.Log;
import com.rancho.web.admin.domain.bo.AdminUserDetails;
import com.rancho.web.admin.domain.dto.admin.AdminLogin;
import com.rancho.web.admin.domain.dto.admin.AdminCreate;
import com.rancho.web.admin.domain.dto.admin.AdminUpdate;
import com.rancho.web.admin.domain.dto.admin.AdminUpdatePassword;
import com.rancho.web.admin.domain.dto.menu.MenuNode;
import com.rancho.web.admin.domain.vo.AdminWithRole;
import com.rancho.web.admin.domain.vo.RouteVo;
import com.rancho.web.admin.service.AdminService;
import com.rancho.web.admin.service.MenuService;
import com.rancho.web.admin.service.RoleService;
import com.rancho.web.common.page.Page;
import com.rancho.web.common.page.PageInfo;
import com.rancho.web.common.result.CommonResult;
import com.rancho.web.common.util.StringUtils;
import com.rancho.web.db.domain.Admin;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Api(value = "管理员管理", tags = "管理员管理")
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private RoleService roleService;

    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${jwt.tokenPrefix}")
    private String tokenPrefix;

    @ApiOperation(value = "登陆", notes = "登陆")
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<CommonResult> login(@Validated @RequestBody AdminLogin adminLogin){
        String token= adminService.login(adminLogin);
        if(StringUtils.isEmpty(token)){
            return ResponseEntity.badRequest().body(CommonResult.badRequest("账号或密码错误"));
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        tokenMap.put("tokenPrefix", tokenPrefix);
        return ResponseEntity.ok(CommonResult.ok(tokenMap));
    }

    @ApiOperation(value = "获取当前登录用户信息")
    @GetMapping("/info")
    @ResponseBody
    public ResponseEntity<CommonResult> info() {
        AdminUserDetails adminUserDetails = (AdminUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Admin admin = adminService.getAdminByUsername(adminUserDetails.getUsername());
        Set<String> roles= roleService.getAdminRoles(admin);
        Set<String> permissions= menuService.getAdminMenuPermissions(admin);
        Map<String, Object> data = new HashMap<>();
        data.put("admin", admin);
        data.put("roles", roles);
        data.put("permissions", permissions);
        return ResponseEntity.ok(CommonResult.ok(data));
    }

    @ApiOperation(value = "获取当前登录管理员路由信息")
    @GetMapping("/route")
    @ResponseBody
    public ResponseEntity<CommonResult> getAdminRoutes() {
        AdminUserDetails adminUserDetails = (AdminUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Admin admin = adminService.getAdminByUsername(adminUserDetails.getUsername());
        List<MenuNode> menuNodes=menuService.getAdminTreeMenus(admin);
        List<RouteVo> routeVos = menuService.menuCovertRoute(menuNodes);
        return ResponseEntity.ok(CommonResult.ok(routeVos));
    }

    @Log("更新管理员密码")
    @ApiOperation(value = "更新管理员密码")
    @PutMapping("/password")
    @ResponseBody
    public ResponseEntity<CommonResult> updateLoginAdminPassword(@Validated(value = Update.class) @RequestBody AdminUpdatePassword adminUpdatePassword) {
        adminService.updateLoginAdminPassword(adminUpdatePassword.getOldPassword(),adminUpdatePassword.getNewPassword());
        return ResponseEntity.ok(CommonResult.ok());
    }

    @ApiOperation(value = "登出功能")
    @PostMapping("/logout")
    @ResponseBody
    public ResponseEntity<CommonResult> logout() {
        AdminUserDetails adminUserDetails = (AdminUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Admin admin = adminService.getAdminByUsername(adminUserDetails.getUsername());
        adminService.logout(admin);
        return ResponseEntity.ok(CommonResult.ok());
    }

    @Log("查询管理员")
    @ApiOperation("管理员列表")
    @GetMapping
    @ResponseBody
    @PreAuthorize("hasAuthority('admin:list')")
    public ResponseEntity<CommonResult<PageInfo<Admin>>> getAdmins(Admin admin, Page page) {
        List<Admin> admins=adminService.getAdmins(admin,page);
        PageInfo<Admin> pageInfo = PageInfo.convertPage(admins);
        return ResponseEntity.ok(CommonResult.ok(pageInfo));
    }

    @Log("添加管理员")
    @ApiOperation(value = "添加管理员")
    @PostMapping
    @ResponseBody
    @PreAuthorize("hasAuthority('admin:add')")
    public ResponseEntity<CommonResult> addAdmin(@Validated({Insert.class}) @RequestBody AdminCreate adminCreate) {
        adminService.addAdmin(adminCreate);
        return ResponseEntity.ok(CommonResult.ok());
    }

    @Log("查询管理员详情")
    @ApiOperation(value = "查询管理员详情")
    @GetMapping("/{id}")
    @ResponseBody
    @PreAuthorize("hasAuthority('admin:detail')")
    public ResponseEntity<CommonResult<AdminWithRole>> getAdminWithRole(@PathVariable Integer id) {
        AdminWithRole adminWithRole = adminService.getAdminWithRole(id);
        return ResponseEntity.ok(CommonResult.ok(adminWithRole));
    }

    @Log("更新管理员")
    @ApiOperation(value = "更新管理员")
    @PutMapping("/{id}")
    @ResponseBody
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<CommonResult> updateAdmin(@PathVariable Integer id, @Validated @RequestBody AdminUpdate adminUpdate) {
        adminService.updateAdmin(id,adminUpdate);
        return ResponseEntity.ok(CommonResult.ok());
    }

    @Log("修改管理员状态")
    @ApiOperation(value = "更新管理员状态")
    @PutMapping("/{id}/status")
    @ResponseBody
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<CommonResult> updateAdminStatus(@PathVariable Integer id,Integer status) {
        adminService.updateAdminStatus(id,status);
        return ResponseEntity.ok(CommonResult.ok());
    }

    @Log("更新管理员密码")
    @ApiOperation(value = "更新管理员密码")
    @PutMapping("/{id}/password")
    @ResponseBody
    public ResponseEntity<CommonResult> updateAdminPassword(@PathVariable  Integer id, @Validated @RequestBody AdminUpdatePassword adminUpdatePassword) {
        adminService.updateAdminPassword(id,adminUpdatePassword.getNewPassword());
        return ResponseEntity.ok(CommonResult.ok());
    }

    @ApiOperation("导出管理员")
    @GetMapping("/download")
    @ResponseBody
    @PreAuthorize("hasAuthority('admin:list')")
    public void download(Admin admin, Page page, HttpServletResponse response){
        try {
            adminService.download(adminService.getAdmins(admin,page),response);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
