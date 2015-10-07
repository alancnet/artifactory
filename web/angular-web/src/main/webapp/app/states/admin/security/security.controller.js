export class AdminSecurityController {
  constructor ($modal) {
   this.userData=this.getUserGridData();
      this.modal= $modal;
  }

    getUserGridData(){

        return[
            {"User Name": "System Info", "Realm": "admin.advanced.system_info", "Admin":true,"Last Login":"12/12/2015","Extrenal Realm Status":"bla bla"},
            {"User Name": "System Info", "Realm": "admin.advanced.system_info", "Admin":true,"Last Login":"12/12/2015","Extrenal Realm Status":"bla bla"},
            {"User Name": "System Info", "Realm": "admin.advanced.system_info", "Admin":true,"Last Login":"12/12/2015","Extrenal Realm Status":"bla bla"},
            {"User Name": "System Info", "Realm": "admin.advanced.system_info", "Admin":true,"Last Login":"12/12/2015","Extrenal Realm Status":"bla bla"}

        ]
    }
    addUser() {
    this.modal.open({template:"<div>test test</div>"})
        this.userData.push({});

    }
}