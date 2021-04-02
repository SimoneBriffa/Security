export class User{

    id: number;
    userId: string;
    firstName: string;
    lastName: string;
    username: string;
    email: string;
    loginDateDisplay: Date;
    joinDate: Date;
    profileImageUrl: string;
    active: boolean;
    notLocked: boolean;
    role: string;
    authorities: [];

    constructor(){
        this.firstName = '';
        this.lastName = '';
        this.username = '';
        this.email = '';
        this.active = true;
        this.notLocked = true;
        this.authorities = [];
    }


}