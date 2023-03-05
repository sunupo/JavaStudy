namespace java com.com.sjq.thrift.demo


/**
* @TypeDoc(
*   description = "测试typedoc"
* )
**/
struct TUser {
        /**
         * @FieldDoc(
         *     description = "用户主键id",
         *     example = {1}
         * )
         */
        1: i64 id;
        /**
         * @FieldDoc(
         *     description = "用户姓名",
         *     example = {test}
         * )
         */
        2: string name;
}


/**
 * @TypeDoc(
 *     description = "HelloWorldService 项目 thrift 异常"
 * )
 */
exception THelloWordServiceException {
        /**
             * @FieldDoc(
             *     description = "错误码",
             *     example = "0"
             * )
             */
        1: i32 code
        /**
                 * @FieldDoc(
                 *     description = "错误消息",
                 *     example = "成功"
                 * )
                 */
        2: string message
}


/**
 * @InterfaceDoc(
 *     displayName = "HelloWordService displayName",
 *     type = "octo.thrift",
 *     scenarios = "",
 *     description = ""
 * )
 */
service HelloWorldTService{
        /**
         * @MethodDoc(
         *     displayName = "sayHello displayName",
         *     description = "sayHello description",
         *     parameters = {
         *         @ParamDoc( name = "userName", description = "用户名", example = {[1]})
         *     },
         *     returnValueDescription = "返回name和age",
         *     exceptions = {
         *         @ExceptionDoc( name = "e", description = "异常")
         *     }
         * )
         */
        string sayHello(1:string userName, 2:i32 age) throws (1:THelloWordServiceException e)
}