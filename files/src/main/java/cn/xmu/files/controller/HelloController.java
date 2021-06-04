package cn.xmu.files.controller;

import cn.xmu.grace.result.GraceJSONResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

@RestController
    public class HelloController  {

        final static Logger logger = LoggerFactory.getLogger(HelloController.class);

        public Object hello() {
            return GraceJSONResult.ok("Hello World!");
        }


    }

