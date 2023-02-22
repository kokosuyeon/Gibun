<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

    <title>기분어때? 심리건강 서비스</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="/css/bootstrap.min.css" rel="stylesheet">
	<link href="/css/goodMind.css" rel="stylesheet">
    <script src="/js/bootstrap.bundle.min.js"></script>
	<script src="https://code.jquery.com/jquery-3.6.3.js" integrity="sha256-nQLuAZGRRcILA+6dMBOvcRh5Pe310sBpanc6+QBmyVM=" crossorigin="anonymous"></script>
    <script src="https://kit.fontawesome.com/77ad8525ff.js" crossorigin="anonymous"></script>
    <script>
    	$(document).ready(function() {
    		$("#profileBtn').click(function(e) {
    			$('#profileInputDisp').attr({'class': 'mt-2'});
    		});
    		$('#profileSubmit').click(function(e) {
    			$('#profileInputDisp').attr({'class': 'mt-2 d-none'});
    			let profileInputVal = $('#profileInput')[0];
    			console.log(profileInputVal.files[0]);
    			const formData = new FormData();
   				formData.append('profile', profileInputVal.files[0]);
   				$.ajax({
   					type: 'POST',
    				url: '/aside/profile',
    				data: formData,
    				processData: false,
    				contentType: false,
    				success: function(result) {
    					const src = '/goodM/file/download?file=' + result;
    					$('#profileImg').attr({src});	// attr({src: src})
    				}
   				});
    		});
    		$('#stateMsgBtn').click(function(e) {
    			$('#stateMsgInput').attr({'class': 'mt-2'});
    			$('#stateInput').attr({value: $('#stateMsg').text()});
    		});
    		$('#stateMsgSubmit').click(function(e) {
    			$('#stateMsgInput').attr({'class': 'mt-2 d-none'});
    			let stateInputVal = $('#stateInput').val();
    			$.ajax({
    				type: 'GET',
    				url: '/aside/stateMsg',
    				data: {stateMsg: stateInputVal},
    				success: function(e) {
    					console.log('state message:', stateInputVal);
    					$('#stateMsg').html(stateInputVal);
    				}
    			});
    		});
    		$('#weather').click(getWeatherInfo);
    		$('#addrChange').click(function(e) {
    			$('#addrInputDisp').attr({'class': 'mt-2'});
    		});
    		$('#addrSubmit').click(function(e) {
    			$('#addrInputDisp').attr({'class': 'mt-2 d-none'});
    			let addrInputVal = $('#addrInput').val();
    			$.ajax({
    				type: 'GET',
    				url: '/aside/address',
    				data: {addr: addrInputVal},
    				success: function(e) {
    					console.log('address:', addrInputVal);
    					$('#addr').html(addrInputVal);
    				}
    			});
    		});
    	});
    	function getWeatherInfo() {
    		$.ajax({
    			type: 'GET',
                url: '/aside/weather',
                data: {'addr': $('#addr').text()},
                success: function(result) {
                    console.log('success');
                    $('#weatherInfo').html(result);
                },
    		});
    	}
    </script>