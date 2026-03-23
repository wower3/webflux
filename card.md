❯ 现在我想支持一个新功能，若后端返回的内容格式如下:\
  {
    "componentType": "card",
    "cardName": "complaintData",
    "displayTitle": "投诉数据查询",
    "cardInfo": [
      {
        "key": "id",
        "label": "工单编号",
        "value": "12345678a"
      },
      {
        "key": "customerName",
        "label": "投诉人姓名",
        "value": "张三"
      },
      {
        "key": "phoneNumber",
        "label": "投诉人电话",
        "value": "12345676789"
      }
    ],
    "buttons": [
      {
        "actionId": "confirm",
        "label": "确定",
        "apiEndpoint": "/api/v1/orders/confirm"
      },
      {
        "actionId": "edit",
        "label": "编辑"
      },
      {
        "actionId": "cancel",
        "label": "取消"
      }
    ]
  }\
  前端接受到card数据后，识别出来要渲染的内容是卡片（"card"）然后渲染出卡片   
  （包含标题，卡片信息（cardInfo中的键值对），按键（buttons中的内容））。\   
  其中若buttons有编辑按键，可以支持修改卡片的键对应的值，确定是使用卡片信息向对应接口发送请求（暂时不用实现），取消是取消操作