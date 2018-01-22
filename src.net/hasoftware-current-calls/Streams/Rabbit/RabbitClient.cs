using NLog;
using RabbitMQ.Client;
using RabbitMQ.Client.MessagePatterns;
using System;

namespace hasoftware_current_calls.Streams.Rabbit
{
   public class RabbitClient
   {
      private static readonly Logger Logger = LogManager.GetCurrentClassLogger();

      public delegate void OnRabbitMessageEvent(string routingKey, byte[] body);

      public event OnRabbitMessageEvent OnRabbitMessage;

      private ConnectionFactory _connectionFactory;
      private IConnection _connection;
      private IModel _model;
      private string _exchange;
      private string _queue;
      private bool _running;
      private string _routingKeys;

      private delegate void ConsumeDelegate();

      public RabbitClient(string hostName, string vhost, string userName, string password, string exchange, string routingKeys)
      {
         _connectionFactory = new ConnectionFactory();
         _connectionFactory.HostName = hostName;
         _connectionFactory.VirtualHost = vhost;
         _connectionFactory.UserName = userName;
         _connectionFactory.Password = password;
         _exchange = exchange;
         _routingKeys = routingKeys;
      }

      public bool StartUp()
      {
         _connection = _connectionFactory.CreateConnection();
         _model = _connection.CreateModel();
         _model.ExchangeDeclare(_exchange, ExchangeType.Topic, true);
         _model.BasicQos(0, 0, false);
         _queue = _model.QueueDeclare();
         foreach (var routingKey in _routingKeys.Split(new[] { ',' }, StringSplitOptions.RemoveEmptyEntries))
         {
            Logger.Debug("Subscribing to [{0}]", routingKey);
            _model.QueueBind(_queue, _exchange, routingKey);
         }
         _running = true;
         ConsumeDelegate consumeDelegate = new ConsumeDelegate(Consume);
         consumeDelegate.BeginInvoke(null, null);
         return true;
      }

      public bool ShutDown()
      {
         _running = false;
         _model.Close();
         _connection.Close();
         return true;
      }

      private void Consume()
      {
         var subscription = new Subscription(_model, _queue, true);
         while (_running)
         {
            var basicDeliverEventArgs = subscription.Next();
            if (OnRabbitMessage != null)
            {
               var routingKey = basicDeliverEventArgs.RoutingKey;
               byte[] body = basicDeliverEventArgs.Body;
               OnRabbitMessage.Invoke(routingKey, body);
            }
         }
      }
   }
}
