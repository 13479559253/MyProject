demo文件夹作为后端的代码采用jdk17，需要在idea中使用jdk17去实现，同时需要创建user和orders数据库用于存储信息在你的电脑上，application.properties上配置你的数据库密码。
其余文件为前端，需要用最新vite脚手架来实现项目实现。
后端主要运用spring boot框架，redis，sql数据库，websocket连接，实现了实时的司机与顾客之间的信息传递与存储。
前端利用路由和后端返回的code，整体进行鉴权和分配页面，包括websocket的创建和信息接收。同时调用高德地图api展示地图，转化经纬度与地址，实时展示司机位置等功能。
需要注意高德地图的key和security需要定义在.env.local里面去进行填写你自己的密钥
<img width="2559" height="1465" alt="屏幕截图 2026-05-15 144836" src="https://github.com/user-attachments/assets/f108025f-87df-4145-845d-d03fc9739e25" />
