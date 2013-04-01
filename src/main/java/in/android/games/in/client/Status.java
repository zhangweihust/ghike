package in.android.games.in.client;

public enum Status {
    SUCESS(0), //操作成功
    FAILED(1),//程序错误
    REPEAT_OPERATE(2),//重复操作

    USER_NOT_LOGIN(101),//用户未登�?    NERVER_LOGINED(102), //用户以前没有登录�?    USER_NOT_EXSIT(103),// 用户不存�?    USER_LOGINED(105),//用户已登�?    TO_FINISH_AUTO_SIGNIN(106),//完成自动注册

    PHONE_USED(201),//手机号已被使�?    PINCODE_ERROR(202),//pin码输入错�?    PHONE_FORMAT_ERROR(203), //手机号格式错�?    SEDN_TEXT_MESSAGE_ERROR(204),//发�?短信失败
    PHONE_EMPTY(205),//手机号为�?    PINCODE_EMPTY(206),//pin码为�?    AIRTEL_SIGNIN(207),
    AIRTEL_SIGNUP(208),
    AIRTEL_SIGNOUT(209),
    CUSTOM_PIN_EMPTY(210),
    CUSTOM_PIN_ERROR(211),

    GAME_NOT_EXSIT(301),//游戏不存�?    GAEE_EDITION_NOT_SUPPORT(302),//游戏版本不支�?    CHALLEGE_REQUEST_ALREADY_SEND(303),//已发送challenge请求
    CHALLENGE_REQUEST_CONTENT_IS_EMPTY(304),//challenge请求内容为空
    GAME_ALREADY_LIKE(305),//已经喜欢过这个游�?    INVITE_REQUEST_CONTENT_IS_EMPTY(305),//�?��玩游戏请求内容为�?    
    NICKNAME_IS_EMPTY(401), //nickname为空
    NICKNAME_LITTLE_WORDS(402),//nickname少于三个
    FIRSTNAME_FORMAT_ERROR(403),//firstname 格式错误
    LASTNAME_FORMAT_ERROR(404),//lastname格式错误
    DATEOFBIRTH_FORMAT_ERROR(405), //date of birth 格式错误
    NICKNAME_ILLEGALL_LETTER(406),//nickname含有非法字符
    NICKNAME_USED(407),//nickname已经被使�?    NICKNAME_MORE_WORDS(410),//nickname多于30�?    FIRSTNAME_MORE_WORDS(411),//firstname多于30�?    LASTNAME_MORE_WORDS(412),//lastname多于30�?    
    PROBLEM_TITLE_BLANK(450),    // title cannot be empty!
    PROBLEM_CONTENT_BLANK(451),  // content cannot be empty!
    PROBLEM_TITLE_TOOLONG(452),  // title should be less than 30 letters.
    PROBLEM_CONTENT_TOOLONG(453),// content should be less than 500 letters.
    CONTENT_ILLEGALL_LETTER(454), //content content contains illegal characters!
    
    HEAD_NOT_EXSIT(501),//头像不存�?    HEAD_FORMAT_ERROR(502),//头像格式不对
    
    FORUM_NOT_EXSIT(511),//论坛不存�?    FORUM_SUBSCRBED(512),//您已经订阅了�?��论坛
    FORUM_SUBSCRBE_ERROR(513),//您订阅论坛失�?    REPLY_SUCCESS(516), //回复成功
    REPLY_FAILURE(517), //回复失败
    
    ALREADY_FRIEND(601),//already friend
    ALREADY_NO_RELATION(602),//already no relation
    
    FRIEND_REQUEST_NOTIFY_NOT_EXIST(701),//friend request not exist!
    CHALLENGE_REQUEST_NOTIFY_NOT_EXIST(702),//challenge request not exist!
    CHALLENGE_RESULT_NOTIFY_NOT_EXIST(703),//CHALLENGE RESULT NOT EXIST!
    
    SUBJECT_MAX(901),
    CONTENT_MAX(902),
    TOPIC_NO_EXISTS(903),
    TOPIC_NO_YOUR_CREATE(904),
    
    // FRIEND 
    
    NO_USER_FIND(1001),
    MYPROFILE_PAGE(1002),
    OTHERPROFILE_PAGE(1003),
    NO_FRIEND_TODEL(1004),
    NO_FRIEND_TOADD(1005),
    NO_INVITE_SELF(1006),
    //already friend
    AlREADY_APPLY(1007),
    
    NOT_ACCEPT_SETTING(1008),
    ;
    
    private int value;
    private Status(int value)
    {
        this.value=value;
    }

    public int getValue() {
        return value;
    }

}
