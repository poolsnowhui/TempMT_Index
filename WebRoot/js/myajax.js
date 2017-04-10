window.onload = function() {
	// 1.网页加载时获取数据库内容
	loadDBList();
	/** ******************************************************** */
	// 2.SQL查询
	document.getElementById("queryatsql").onclick = function() {
		query();
	}
	// 3.全表建磁盘索引
	document.getElementById("diskCreateLob").onclick = function() {
		createAndSaveDisk();
	}
}
// 查询
function query() {
	// 1.创建AJAX
	// alert("// 1.创建AJAX")
	var xmlhttp = ajaxFunction();
	xmlhttp.onreadystatechange = function() {
		// alert(xmlhttp.readyState);
		if (xmlhttp.readyState == 4) {
			// alert(xmlhttp.status);
			if (xmlhttp.status == 200 || xmlhttp.status == 304) {
				// 3.回应
				// alert("// 3.回应")
				// 3.1回应JSON
				var data = xmlhttp.responseText;
				var dataParase = JSON.parse(data);// 解析json
				var tablecontent;
				var dataSplit;
				// 3.2解析JSON表格段
				if (tablecontent = dataParase.tableContent[0]) {
					dataSplit = tablecontent.split(",");
					// 输出表
					var attrNum = dataSplit[0];
					var recordNum = 0;
					if (attrNum != 0 && attrNum != null) {
						recordNum = (dataSplit.length - attrNum - 1) / attrNum;
					}

					var data = [];// 数据
					for (var i = 0; i < recordNum; i++) {// 行数
						var temp = [];
						for (var j = 0; j < attrNum; j++) {// 列数
							var tempNum = (1 + i) * attrNum + 1 + j;// 行列算出位置
							temp[j] = dataSplit[tempNum];
						}
						data[i] = temp;
					}
					var columns = []; // 列名
					for (var j = 0; j < attrNum; j++) {
						columns[j] = dataSplit[1 + j];
					}
					var cs = new table({
						"tableId" : "cs_table", // 必须
						// "headers":[dataSplit[1],dataSplit[2],dataSplit[3],dataSplit[4],dataSplit[5]],
						"headers" : columns, // 必须
						"data" : data, // 必须
						"displayNum" : 10, // 必须 默认 10
						"groupDataNum" : 10
					// 可选 默认 10
					});
				}
				// 3.3解析JSON提示信息
				var p = document.getElementById("prompt");
				p.getElementsByTagName("label")[0].innerHTML = dataParase.atsql[0];
				p.getElementsByTagName("label")[1].innerHTML = dataParase.sql[0];
				p.getElementsByTagName("label")[2].innerHTML = dataParase.recordNum[0]
						+ "条";
				p.getElementsByTagName("label")[3].innerHTML = dataParase.timeExpense[0]
						+ "ms";
				p.getElementsByTagName("label")[4].innerHTML = dataParase.type[0];
				p.getElementsByTagName("label")[5].innerHTML = dataParase.stragey[0];
				p.getElementsByTagName("label")[6].innerHTML = dataParase.errMessage[0];

			}
		}
	}
	// 2.请求
	// alert("// 2.请求")
	var atsql = document.getElementById("atsql").value;
	var lob = document.getElementById("lob").value;
	var isFromDisk = document.getElementById("isFromDisk").value;
	// alert("isFromDisk"+isFromDisk)
	xmlhttp.open("post", "/TempMT_Index/QueryServlet?timeStamp="
			+ new Date().getTime(), true);
	xmlhttp.setRequestHeader("Content-Type",
			"application/x-www-form-urlencoded");
	xmlhttp
			.send("atsql=" + atsql + "&lob=" + lob + "&isFromDisk="
					+ isFromDisk);
}
// 整张时态表建索引保存在磁盘内
function createAndSaveDisk() {
	var xmlhttp = ajaxFunction();
	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
			// alert("网页回应");
			var data = xmlhttp.responseText;
			// alert(data);
			var dataParase = JSON.parse(data);// 解析json
			// 获取提示信息
			// 3.3解析JSON提示信息
			var message = "当前ATSQL语句: ";
			message = message + dataParase.atsql[0];
			var p1 = document.createElement("p");
			p1.appendChild(document.createTextNode(message));

			message = "当前提示信息: ";
			message += dataParase.message[0];
			var p2 = document.createElement("p");
			p2.setAttribute("class", "red");
			p2.appendChild(document.createTextNode(message));

			message = "当前查询总记录: ";
			message += recordNum;
			var p3 = document.createElement("p");
			p3.appendChild(document.createTextNode(message));

			message = "当前时间开销: ";
			message += dataParase.expense[0];
			message += "ms";
			var p4 = document.createElement("p");
			p4.appendChild(document.createTextNode(message));

			message = "当前索引类型: ";
			message += dataParase.indexType[0];
			var p5 = document.createElement("p");
			p5.appendChild(document.createTextNode(message));

			var prompt = document.getElementById("prompt");
			prompt.innerHTML = "";
			prompt.appendChild(p1);
			prompt.appendChild(p2);
			prompt.appendChild(p3);
			prompt.appendChild(p4);
			prompt.appendChild(p5);
		}
	}
	xmlhttp.open("GET", "/TempMT_Index/CreateLOBIndexServlet?timeStamp="
			+ new Date().getTime(), true);
	xmlhttp.send();
}
// 加载当前数据库表
function loadDBList() {
	var xmlhttp = ajaxFunction();
	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
			// alert("网页回应");
			var data = xmlhttp.responseText;
			// alert(data);
			var dataParase = JSON.parse(data);// 解析json

			var ul = document.getElementById("currentDBList");// 获取html中的无序列表
			var liMode = document.getElementById("currentPerList");// 获取html中的第一列
			var aMode = liMode.getElementsByTagName("a")[0];
			// ul.innerHTML = "";// 初始化
			// alert(dataParase.tableName[0]);
			// alert(dataParase.tableName[0][0]);
			for (var int = 0; int < dataParase.tableName[0].length; int++) {
				var li = document.createElement("li");
				var a = aMode.cloneNode();
				a.appendChild(document
						.createTextNode(dataParase.tableName[0][int]));
				li.appendChild(a);
				ul.appendChild(li);
			}
		}
	}
	xmlhttp.open("GET",
			"/TempMT_Index/ShowServlet?timeStamp=" + new Date().getTime(), true);
	xmlhttp.send();
}

// 创建AJAX
function ajaxFunction() {
	var xmlhttp;
	if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		xmlhttp = new XMLHttpRequest();
	} else {// code for IE6, IE5
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}
	return xmlhttp;
}
