package com.example.wsTextEditor.controller;

import com.example.wsTextEditor.model.ActionLog;
import com.example.wsTextEditor.model.Document;
import com.example.wsTextEditor.model.User;
import com.example.wsTextEditor.model.DocumentCollaborator;
import com.example.wsTextEditor.pojo.CollaboratorInfo;
import com.example.wsTextEditor.repository.DocumentCollaboratorRepository;
import com.example.wsTextEditor.repository.DocumentRepository;
import com.example.wsTextEditor.repository.UserRepository;
import com.example.wsTextEditor.service.DocumentPermissionService;
import com.example.wsTextEditor.model.DocumentCollaborator.PermissionLevel;
import com.example.wsTextEditor.repository.ActionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 编辑器控制器
 * 处理编辑器页面的显示、文档创建和加入等操作
 */
@Controller
public class EditorController {

    /** 文档仓库，用于数据库操作 */
    private final DocumentRepository documentRepository;
    /** 用户仓库，用于用户相关数据库操作 */
    private final UserRepository userRepository;
    /** 文档权限服务，用于处理文档访问权限 */
    private final DocumentPermissionService documentPermissionService;
    /** 文档协作者仓库，用于协作者相关数据库操作 */
    private final DocumentCollaboratorRepository documentCollaboratorRepository;
    /** 操作日志仓库，用于记录用户操作 */
    private final ActionLogRepository actionLogRepository;
    
    @Value("${y.websocket.url}")
    private String yWebsocketUrl;

    /**
     * 构造函数，通过依赖注入初始化所需的仓库和服务
     * @param documentRepository 文档仓库
     * @param userRepository 用户仓库
     * @param documentPermissionService 文档权限服务
     * @param documentCollaboratorRepository 文档协作者仓库
     * @param actionLogRepository 操作日志仓库
     */
    @Autowired
    public EditorController(DocumentRepository documentRepository, 
                           UserRepository userRepository,
                           DocumentPermissionService documentPermissionService,
                           DocumentCollaboratorRepository documentCollaboratorRepository,
                           ActionLogRepository actionLogRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.documentPermissionService = documentPermissionService;
        this.documentCollaboratorRepository = documentCollaboratorRepository;
        this.actionLogRepository = actionLogRepository;
    }

    /**
     * 显示仪表板页面
     * @param model Spring MVC模型对象
     * @param userDetails 当前认证用户信息
     * @param loginSuccess 登录成功标志
     * @param tagsParam 标签筛选参数
     * @return dashboard视图名称
     */
    @GetMapping("/")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails, 
                          @RequestParam(value = "loginSuccess", required = false) String loginSuccess,
                          @RequestParam(value = "tags", required = false) String tagsParam) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        //获取用户信息，并且获得用户参与的文档，通过流操作
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        
        List<Document> documents;
        if (tagsParam != null && !tagsParam.isEmpty()) {
            // 如果有标签参数，按标签筛选文档
            String[] tags = tagsParam.split(",");
            Set<Document> documentSet = new HashSet<>();
            
            for (String tag : tags) {
                String trimmedTag = tag.trim();
                if (!trimmedTag.isEmpty()) {
                    // 使用新的查询方法来匹配包含标签的文档
                    List<Document> docsByTag = documentRepository.findByTagContaining( trimmedTag);
                    documentSet.addAll(docsByTag);
                }
            }
            
            documents = new ArrayList<>(documentSet);
        } else {
            // 没有标签参数，正常加载所有文档
            List<DocumentCollaborator> collaborators = documentCollaboratorRepository.findByUser(user);
            documents = collaborators.stream()
                    .map(DocumentCollaborator::getDocument)
                    .collect(Collectors.toList());
        }
        
        model.addAttribute("documents", documents);
        model.addAttribute("currentUser", user); // 添加当前用户信息到model中
        model.addAttribute("loginSuccess", loginSuccess); // 添加登录成功标志
        
        // 添加标签参数到模型中，用于前端显示
        model.addAttribute("tagsFilter", tagsParam);
        
        return "dashboard";
    }

    /**
     * 创建新文档
     * @param userDetails 当前认证用户信息
     * @param redirectAttributes 重定向属性，用于传递消息
     * @return 重定向到仪表板页面
     */
    @PostMapping("/documents/create")
    public String createDocument(@AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Document doc = new Document();
        doc.setOwner(user);
        doc.setTitle("Untitled Document");
        doc.setLanguage("javascript");
        doc.setContent("// Welcome to Code Share!\n// Start typing your code here...\n\n");
        documentRepository.save(doc);
        
        // 初始化文档权限
        documentPermissionService.initializeDocumentPermissions(doc, user);
        
        // 添加成功消息
        redirectAttributes.addFlashAttribute("message", "Document created successfully!");
        
        // 返回原页面
        return "redirect:/";
    }

    /**
     * 加入已有文档
     * @param documentId 文档ID
     * @param userDetails 当前认证用户信息
     * @param redirectAttributes 重定向属性，用于传递错误消息
     * @return 重定向到编辑器页面或仪表板页面
     */
    @PostMapping("/documents/join")
    public String joinDocument(@RequestParam String documentId, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        // Check if document exists
        if (documentRepository.findByUniqueId(documentId).isPresent()) {
            return "redirect:/editor/" + documentId;
        } else {
            redirectAttributes.addFlashAttribute("error", "Document not found. Please check the document ID.");
            return "redirect:/";
        }
    }

    /**
     * 显示文档编辑器页面
     * @param documentId 文档ID
     * @param model Spring MVC模型对象
     * @param userDetails 当前认证用户信息
     * @param redirectAttributes 重定向属性，用于传递错误消息
     * @return editor视图名称
     */
    @GetMapping("/editor/{documentId}")
    public String editor(@PathVariable String documentId, Model model, @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
        System.out.println("EditorController: userDetails=" + userDetails); // 调试信息
        
        if (userDetails == null) {
            System.out.println("EditorController: userDetails is null, redirecting to login"); // 调试信息
            return "redirect:/login";
        }

        // 检查文档是否存在
        Optional<Document> documentOpt = documentRepository.findByUniqueId(documentId);
        if (documentOpt.isEmpty()) {
            // 如果文档不存在，重定向到主页并显示错误消息
            redirectAttributes.addFlashAttribute("error", "Document not found.");
            return "redirect:/";
        }
        
        Document document = documentOpt.get();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        System.out.println("EditorController: user=" + user); // 调试信息
        
        // 检查用户是否有权限查看文档
        if (!documentPermissionService.canViewDocument(document, user)) {
            // 如果用户没有权限，重定向到主页并显示错误消息
            redirectAttributes.addFlashAttribute("error", "You don't have permission to access this document.");
            return "redirect:/";
        }

        model.addAttribute("document", document);
        model.addAttribute("ywsUrl", yWebsocketUrl);
        model.addAttribute("currentUser", user);
        
        // 添加用户权限信息
        PermissionLevel permissionLevel = documentPermissionService.getUserPermissionLevel(document, user);
        model.addAttribute("userPermission", permissionLevel);
        model.addAttribute("canEdit", documentPermissionService.canEditDocument(document, user));
        
        return "editor";
    }
    
    /**
     * 查看操作日志
     * @param model Spring MVC模型对象
     * @param userDetails 当前认证用户信息
     * @return logs视图名称或重定向到仪表板
     */
    @GetMapping("/logs")
    public String viewOperationLogs(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return "redirect:/login";
            }
            
            User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<ActionLog> logs = actionLogRepository.findByUsernameOrderByCreatedAtDesc(user.getUsername());
            
            model.addAttribute("currentUser", user);
            model.addAttribute("logs", logs);
            
            return "logs";
        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();
            // Add an error message to the model and return to dashboard
            model.addAttribute("error", "Unable to load operation logs at this time.");
            return "redirect:/";
        }
    }

    /**
     * 显示任务页面
     * @return tasks视图名称
     */
    @GetMapping("/tasks")
    public String showTasksPage() {
        return "tasks"; // 返回tasks.html模板
    }

}