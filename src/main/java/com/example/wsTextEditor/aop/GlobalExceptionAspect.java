package com.example.wsTextEditor.aop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;         // 1. 手动导入 Logger
import org.slf4j.LoggerFactory;  // 2. 手动导入 LoggerFactory
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 全局异常处理切面类
 * 作用：统一拦截Controller层抛出的异常，并记录详细的日志信息，便于问题排查和系统监控
 */
@Aspect
@Component
public class GlobalExceptionAspect {

    // 4. 【关键修改】手动创建 Logger 对象，替代 @Slf4j
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionAspect.class);

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 定义切点：拦截com.example.wsTextEditor.controller包及其子包下的所有方法
     * 该切点用于匹配所有Controller层的方法执行
     */
    @Pointcut("execution(* com.example.wsTextEditor.controller..*.*(..))")
    public void controllerPointcut() {}

    /**
     * 异常通知方法：当目标方法抛出异常时执行
     * 该方法会捕获Controller层所有未处理的异常，并记录详细的上下文信息
     * @param joinPoint 连接点对象，包含方法执行的相关信息（如类名、方法名、参数等）
     * @param e 抛出的异常对象
     */
    @AfterThrowing(pointcut = "controllerPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        try {
            // 获取当前请求的Servlet属性，用于提取HTTP请求相关信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                // 获取HTTP请求对象
                HttpServletRequest request = attributes.getRequest();
                // 将请求URL放入MDC（Mapped Diagnostic Context）中，便于日志追踪
                MDC.put("request_url", request.getRequestURL().toString());
                // 将请求方法（GET/POST等）放入MDC中
                MDC.put("request_method", request.getMethod());
                // 将客户端IP地址放入MDC中
                MDC.put("client_ip", getIpAddress(request));
            }

            // 生成或获取跟踪ID，用于关联同一请求的所有日志，方便问题排查时定位完整请求链路
            String traceId = MDC.get("traceId");
            if (traceId == null || traceId.isEmpty()) {
                traceId = UUID.randomUUID().toString().replace("-", "");
                MDC.put("traceId", traceId);
            }

            // 获取发生异常的类名和方法名，用于精确定位异常发生的位置
            String className = joinPoint.getSignature().getDeclaringTypeName();
            String methodName = joinPoint.getSignature().getName();

            // 序列化方法参数为JSON字符串，记录方法调用时的具体参数值
            String argsJson = "";
            try {
                // 创建可序列化的参数列表，过滤掉不能序列化的对象（如ServletRequest等）
                List<Object> serializableArgs = new ArrayList<>();
                for (Object arg : joinPoint.getArgs()) {
                    // 过滤掉ServletRequest、ServletResponse、MultipartFile和BindingResult等不能序列化的对象
                    // 这些对象通常包含大量无关信息且可能引发序列化异常
                    if (arg instanceof ServletRequest ||
                            arg instanceof ServletResponse ||
                            arg instanceof MultipartFile ||
                            arg instanceof BindingResult) {
                        continue;
                    }
                    serializableArgs.add(arg);
                }
                // 使用ObjectMapper将参数列表转换为JSON字符串，便于查看和分析
                argsJson = objectMapper.writeValueAsString(serializableArgs);
            } catch (JsonProcessingException jsonEx) {
                // 如果序列化失败，记录警告日志，但不影响主流程执行
                log.warn("Jackson序列化参数失败: {}", jsonEx.getMessage());
                argsJson = "参数序列化失败";
            }

            // 将类名、方法名、方法参数和异常类名放入MDC中，这些信息会被自动附加到日志输出中
            MDC.put("class_name", className);
            MDC.put("method_name", methodName);
            MDC.put("method_args", argsJson);
            MDC.put("exception_class", e.getClass().getName());

            // 记录错误日志，包含类名、方法名和异常信息，最后一个参数e用于输出异常堆栈
            log.error("在{}.{}方法中发生异常: {}", className, methodName, e.getMessage(), e);

        } catch (Exception ex) {
            // 如果在记录异常日志过程中发生错误，记录该错误，防止日志记录过程影响主业务流程
            log.error("全局异常切面记录日志时发生错误", ex);
        } finally {
            // 清理MDC中的数据，防止线程复用时数据污染，这是非常重要的步骤
            MDC.clear();
        }
    }

    /**
     * 获取客户端真实IP地址
     * 考虑了代理服务器的情况，优先从X-Forwarded-For头部获取真实IP
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        // 尝试从X-Forwarded-For头部获取IP地址，这是标准的代理服务器转发头部
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            // 如果X-Forwarded-For头部为空，则尝试从RemoteAddr获取
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}