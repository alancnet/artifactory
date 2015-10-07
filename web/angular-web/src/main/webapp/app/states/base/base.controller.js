export class BaseController {
    constructor(FooterDao) {
        // Ensure page is not displayed before we get the footer data
        FooterDao.get(true).then(footerData => this.footerData = footerData);
    }
}