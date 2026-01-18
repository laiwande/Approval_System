package cn.edu.shiep.backend.approvalsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import java.util.List;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
}

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        ).exceptionHandling(exceptions ->
        exceptions.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .authorizeHttpRequests(authz -> authz
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 登录和注册
                .requestMatchers("/api/auth/**").permitAll()

                // 文件上传和下载 - 需要登录
                .requestMatchers("/api/files/**").hasAnyRole("EMPLOYEE", "APPROVER", "ADMIN")
                .requestMatchers("/uploads/**").permitAll() // 静态文件访问

                // 申请相关 - 所有登录用户都可以创建和查看自己的申请
                .requestMatchers(HttpMethod.POST, "/api/applies").hasAnyRole("EMPLOYEE", "APPROVER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/applies/my").hasAnyRole("EMPLOYEE", "APPROVER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/applies/{id}").hasAnyRole("EMPLOYEE", "APPROVER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/applies/{id}/submit").hasAnyRole("EMPLOYEE", "APPROVER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/applies/{id}/withdraw").hasAnyRole("EMPLOYEE", "APPROVER", "ADMIN")

                // 审批相关 - 审批人和管理员可以处理审批
                .requestMatchers(HttpMethod.GET, "/api/approvals/tasks/pending").hasAnyRole("APPROVER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/approvals/tasks/process").hasAnyRole("APPROVER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/approvals/records/my").hasAnyRole("APPROVER", "ADMIN")
                
                // 管理员功能 - 查看所有申请、管理审批流程
                .requestMatchers(HttpMethod.GET, "/api/approvals/all").hasRole("ADMIN")
                .requestMatchers("/api/approval-processes/**").hasRole("ADMIN")
                
                // 系统管理 - 用户、部门、岗位管理
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                .anyRequest().authenticated()
        );


        return http.build();
    }
}