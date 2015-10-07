/**
 * Created by idannaim on 8/2/15.
 */
let artifactoryNotifications;
let toast;
let timeout;
describe('Unit test: ArtifactoryNotifications', () => {


    beforeEach(m('artifactory.services'));
    beforeEach(inject(($timeout, ArtifactoryNotifications, toaster) => {
        toast = toaster;
        timeout = $timeout;
        spyOn(toast, 'pop');
        artifactoryNotifications = ArtifactoryNotifications;
    }));

    it('should pop success message with options', ()=> {
        let messageOptions = {
            type: 'success',
            timeout: 5000,
            body: 'my message success',
            showCloseButton: true,
            clickHandler: artifactoryNotifications.notifClickHandle
        };
        let message = {
            info: 'my message success'
        };
        artifactoryNotifications.create(message);
        expect(toast.pop).toHaveBeenCalledWith(messageOptions);
    });

    it('should pop error message with options', ()=> {
        let message = {error: 'my error message'};
        let messageOptions = {
            type: 'error',
            timeout: 10000,
            body: 'my error message',
            showCloseButton: true,
            clickHandler: artifactoryNotifications.notifClickHandle
        };
        artifactoryNotifications.create(message);
        expect(toast.pop).toHaveBeenCalledWith(messageOptions);
    });

    it('should pop warn message with options', ()=> {
        let messageOptions = {
            type: 'warning',
            timeout: 4000,
            body: 'my message warn',
            showCloseButton: true,
            clickHandler: artifactoryNotifications.notifClickHandle
        };
        let message = {
            warn: 'my message warn'
        };
        artifactoryNotifications.create(message);
        expect(toast.pop).toHaveBeenCalledWith(messageOptions);
    });

    it('should pop message with html template', ()=> {
        let tempHtml = {
            type: 'success',
            body: 'test content',
            timeout: 60 * 60000
        };
        artifactoryNotifications.createMessageWithHtml(tempHtml);
        let tempHtmlOptions = {
            type: tempHtml.type,
            body: tempHtml.body,
            bodyOutputType: 'trustedHtml',
            timeout: tempHtml.timeout,
            showCloseButton: true,
            clickHandler: artifactoryNotifications.notifClickHandle
        };
        expect(toast.pop).toHaveBeenCalledWith(tempHtmlOptions);
    });

    it('should clear all toasts', ()=> {
        spyOn(toast, 'clear');
        artifactoryNotifications.clear();
        expect(toast.clear).toHaveBeenCalled();
    });

    it('should not have duplicate  toast ', ()=> {
        let message = {warn: 'my message warn'};
        artifactoryNotifications.create(message);
        artifactoryNotifications.create(message);
        expect(toast.pop.calls.count()).toEqual(1);
    });
    it('should show next toast when timeout finish ', ()=> {
        let message = {warn: 'my message warn'};
        artifactoryNotifications.create(message);
        timeout.flush();
        artifactoryNotifications.create(message);
        expect(toast.pop.calls.count()).toEqual(2);
    });
});