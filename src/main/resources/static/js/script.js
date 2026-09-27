
const toggleSidebar =()=>{
	
	if($('.sidebar').is(":visible")){
		
		$(".sidebar").css("display","none")
		$(".content").css("margin-left","0%")
		
	}else{
		
		$(".sidebar").css("display","block")
		$(".content").css("margin-left","20%")	
		
	}
	
}


//search functionality
const search=() => {
	let query=$('#searchInput').val();
	if(query==''){
		$('.search-result').hide();
	}else{
		//sending request to server
		let url=`http://localhost:8080/search/${query}`;
		fetch(url).then(response=>{
			return response.json();
		}).then((data)=>{
			
			let text=`<div class='list-group'>`
					data.forEach((contacts) =>{
						text+=`<a href='/user/${contacts.cid}/contact' class='list-group-item list-group-item-action'>
									${contacts.name}
							   </a>`
					})
				text+=`</div>`	
			$(".search-result").html(text);	
			$('.search-result').show();	
		}).catch((e)=>{
			console.error(e);
		});
	}
}
