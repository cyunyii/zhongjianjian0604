package cn.xmu.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@SpringBootApplication(scanBasePackages = {"cn.xmu","cn.xmu.admin"})
@MapperScan("cn.xmu.admin.mapper")
@ComponentScan(basePackages = {"cn.xmu", "org.n3r.idworker"})
public class Application {
    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }
}
