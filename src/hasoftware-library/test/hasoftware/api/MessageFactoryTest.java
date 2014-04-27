package hasoftware.api;

import hasoftware.api.classes.CurrentEvent;
import hasoftware.api.classes.Point;
import hasoftware.api.messages.CurrentEventResponse;
import hasoftware.api.messages.LoginRequest;
import hasoftware.api.messages.NotifyRequest;
import hasoftware.cdef.CDEFAction;
import org.junit.Test;

public class MessageFactoryTest {

    public MessageFactoryTest() {
    }

    @Test
    public void testJsonEncode() {
        {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername("username");
            loginRequest.setPassword("password");

            String json = JSONMessageFactory.encode(loginRequest);
            System.out.println(json);

            Message decoded = JSONMessageFactory.decode(json);
            //System.out.println(decoded);
        }

        {
            NotifyRequest notifyRequest = new NotifyRequest();
            notifyRequest.getFunctionCodes().add(FunctionCode.CurrentEvent);

            String json = JSONMessageFactory.encode(notifyRequest);
            System.out.println(json);

            Message decoded = JSONMessageFactory.decode(json);
            //System.out.println(decoded);
        }

        {
            CurrentEventResponse currentEventResponse = new CurrentEventResponse(99);
            currentEventResponse.setAction(CDEFAction.List);
            Point point = new Point();
            point.setId(1000);
            point.setNodeId(0);
            point.setName("Point");
            point.setAddress("1.101");
            point.setDeviceTypeCode("CODE");
            point.setMessage1("Point Message 1");
            point.setMessage2("Point Message 2");
            point.setPriority(0);
            point.setCreatedOn(123456);
            point.setUpdatedOn(123456);

            CurrentEvent currentEvent = new CurrentEvent();
            currentEvent.setId(100);
            currentEvent.setPoint(point);
            currentEvent.setCreatedOn(123456);
            currentEvent.setUpdatedOn(123456);

            currentEventResponse.getCurrentEvents().add(currentEvent);

            String json = JSONMessageFactory.encode(currentEventResponse);
            System.out.println(json);

            Message decoded = JSONMessageFactory.decode(json);
            //System.out.println(decoded);
        }
    }
}
