package com.example.wsTextEditor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;


/**
 * WebSocket文本编辑器应用程序主类
 * 启动Spring Boot应用程序并启用事务管理
 */
@SpringBootApplication
@EnableTransactionManagement
public class WsTextEditorApplication {

	/**
	 * 应用程序入口点
	 * @param args 命令行参数
	 */
	public static void main(String[] args) {
		SpringApplication.run(WsTextEditorApplication.class, args);
	}
}