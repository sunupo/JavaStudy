package com.soecode.lyf.web;

import com.java.sjq.base.JNDI.TomcatJNDI;
import com.java.sjq.base.JNDI.User;
import com.java.sjq.base.JNDI.demo1.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/jndi") // url:/模块/资源/{id}/细分 /seckill/list
public class TomcatJNDIController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * GET http://localhost:8080/ssm_war_exploded2/jndi/testJNDI?name=jndi/user&id=1
     * @return
     */
    @RequestMapping(value = "/testJNDI", method = RequestMethod.GET)
    @ResponseBody
    private void testJSON(@RequestParam("name") String name, @RequestParam("id")Integer id){
        System.out.println(name+"---"+id);
        TomcatJNDI.runTomcatJNDI(name, id);
    }

}
