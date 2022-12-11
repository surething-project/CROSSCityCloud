/**
 * @fileoverview
 * @enhanceable
 * @suppress {messageConventions} JS Compiler reports an error if a variable or
 *     field starts with 'MSG_' and isn't a translatable message.
 * @public
 */
// GENERATED CODE -- DO NOT EDIT!

var jspb = require('google-protobuf');
var goog = jspb;
var global = Function('return this')();

goog.exportSymbol('proto.pt.ulisboa.tecnico.cross.contract.Authorization', null, global);
goog.exportSymbol('proto.pt.ulisboa.tecnico.cross.contract.Credentials', null, global);
goog.exportSymbol('proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity', null, global);

/**
 * Generated by JsPbCodeGenerator.
 * @param {Array=} opt_data Optional initial data array, typically from a
 * server response, or constructed directly in Javascript. The array is used
 * in place and becomes part of the constructed object. It is not cloned.
 * If no data is provided, the constructed object will be empty, but still
 * valid.
 * @extends {jspb.Message}
 * @constructor
 */
proto.pt.ulisboa.tecnico.cross.contract.Credentials = function(opt_data) {
  jspb.Message.initialize(this, opt_data, 0, -1, null, null);
};
goog.inherits(proto.pt.ulisboa.tecnico.cross.contract.Credentials, jspb.Message);
if (goog.DEBUG && !COMPILED) {
  proto.pt.ulisboa.tecnico.cross.contract.Credentials.displayName = 'proto.pt.ulisboa.tecnico.cross.contract.Credentials';
}


if (jspb.Message.GENERATE_TO_OBJECT) {
/**
 * Creates an object representation of this proto suitable for use in Soy templates.
 * Field names that are reserved in JavaScript and will be renamed to pb_name.
 * To access a reserved field use, foo.pb_<name>, eg, foo.pb_default.
 * For the list of reserved names please see:
 *     com.google.apps.jspb.JsClassTemplate.JS_RESERVED_WORDS.
 * @param {boolean=} opt_includeInstance Whether to include the JSPB instance
 *     for transitional soy proto support: http://goto/soy-param-migration
 * @return {!Object}
 */
proto.pt.ulisboa.tecnico.cross.contract.Credentials.prototype.toObject = function(opt_includeInstance) {
  return proto.pt.ulisboa.tecnico.cross.contract.Credentials.toObject(opt_includeInstance, this);
};


/**
 * Static version of the {@see toObject} method.
 * @param {boolean|undefined} includeInstance Whether to include the JSPB
 *     instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @param {!proto.pt.ulisboa.tecnico.cross.contract.Credentials} msg The msg instance to transform.
 * @return {!Object}
 * @suppress {unusedLocalVariables} f is only used for nested messages
 */
proto.pt.ulisboa.tecnico.cross.contract.Credentials.toObject = function(includeInstance, msg) {
  var f, obj = {
    username: jspb.Message.getFieldWithDefault(msg, 1, ""),
    password: jspb.Message.getFieldWithDefault(msg, 2, ""),
    cryptoidentity: (f = msg.getCryptoidentity()) && proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity.toObject(includeInstance, f)
  };

  if (includeInstance) {
    obj.$jspbMessageInstance = msg;
  }
  return obj;
};
}


/**
 * Deserializes binary data (in protobuf wire format).
 * @param {jspb.ByteSource} bytes The bytes to deserialize.
 * @return {!proto.pt.ulisboa.tecnico.cross.contract.Credentials}
 */
proto.pt.ulisboa.tecnico.cross.contract.Credentials.deserializeBinary = function(bytes) {
  var reader = new jspb.BinaryReader(bytes);
  var msg = new proto.pt.ulisboa.tecnico.cross.contract.Credentials;
  return proto.pt.ulisboa.tecnico.cross.contract.Credentials.deserializeBinaryFromReader(msg, reader);
};


/**
 * Deserializes binary data (in protobuf wire format) from the
 * given reader into the given message object.
 * @param {!proto.pt.ulisboa.tecnico.cross.contract.Credentials} msg The message object to deserialize into.
 * @param {!jspb.BinaryReader} reader The BinaryReader to use.
 * @return {!proto.pt.ulisboa.tecnico.cross.contract.Credentials}
 */
proto.pt.ulisboa.tecnico.cross.contract.Credentials.deserializeBinaryFromReader = function(msg, reader) {
  while (reader.nextField()) {
    if (reader.isEndGroup()) {
      break;
    }
    var field = reader.getFieldNumber();
    switch (field) {
    case 1:
      var value = /** @type {string} */ (reader.readString());
      msg.setUsername(value);
      break;
    case 2:
      var value = /** @type {string} */ (reader.readString());
      msg.setPassword(value);
      break;
    case 3:
      var value = new proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity;
      reader.readMessage(value,proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity.deserializeBinaryFromReader);
      msg.setCryptoidentity(value);
      break;
    default:
      reader.skipField();
      break;
    }
  }
  return msg;
};


/**
 * Serializes the message to binary data (in protobuf wire format).
 * @return {!Uint8Array}
 */
proto.pt.ulisboa.tecnico.cross.contract.Credentials.prototype.serializeBinary = function() {
  var writer = new jspb.BinaryWriter();
  proto.pt.ulisboa.tecnico.cross.contract.Credentials.serializeBinaryToWriter(this, writer);
  return writer.getResultBuffer();
};


/**
 * Serializes the given message to binary data (in protobuf wire
 * format), writing to the given BinaryWriter.
 * @param {!proto.pt.ulisboa.tecnico.cross.contract.Credentials} message
 * @param {!jspb.BinaryWriter} writer
 * @suppress {unusedLocalVariables} f is only used for nested messages
 */
proto.pt.ulisboa.tecnico.cross.contract.Credentials.serializeBinaryToWriter = function(message, writer) {
  var f = undefined;
  f = message.getUsername();
  if (f.length > 0) {
    writer.writeString(
      1,
      f
    );
  }
  f = message.getPassword();
  if (f.length > 0) {
    writer.writeString(
      2,
      f
    );
  }
  f = message.getCryptoidentity();
  if (f != null) {
    writer.writeMessage(
      3,
      f,
      proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity.serializeBinaryToWriter
    );
  }
};


/**
 * optional string username = 1;
 * @return {string}
 */
proto.pt.ulisboa.tecnico.cross.contract.Credentials.prototype.getUsername = function() {
  return /** @type {string} */ (jspb.Message.getFieldWithDefault(this, 1, ""));
};


/** @param {string} value */
proto.pt.ulisboa.tecnico.cross.contract.Credentials.prototype.setUsername = function(value) {
  jspb.Message.setProto3StringField(this, 1, value);
};


/**
 * optional string password = 2;
 * @return {string}
 */
proto.pt.ulisboa.tecnico.cross.contract.Credentials.prototype.getPassword = function() {
  return /** @type {string} */ (jspb.Message.getFieldWithDefault(this, 2, ""));
};


/** @param {string} value */
proto.pt.ulisboa.tecnico.cross.contract.Credentials.prototype.setPassword = function(value) {
  jspb.Message.setProto3StringField(this, 2, value);
};


/**
 * optional CryptoIdentity cryptoIdentity = 3;
 * @return {?proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity}
 */
proto.pt.ulisboa.tecnico.cross.contract.Credentials.prototype.getCryptoidentity = function() {
  return /** @type{?proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity} */ (
    jspb.Message.getWrapperField(this, proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity, 3));
};


/** @param {?proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity|undefined} value */
proto.pt.ulisboa.tecnico.cross.contract.Credentials.prototype.setCryptoidentity = function(value) {
  jspb.Message.setWrapperField(this, 3, value);
};


proto.pt.ulisboa.tecnico.cross.contract.Credentials.prototype.clearCryptoidentity = function() {
  this.setCryptoidentity(undefined);
};


/**
 * Returns whether this field is set.
 * @return {!boolean}
 */
proto.pt.ulisboa.tecnico.cross.contract.Credentials.prototype.hasCryptoidentity = function() {
  return jspb.Message.getField(this, 3) != null;
};



/**
 * Generated by JsPbCodeGenerator.
 * @param {Array=} opt_data Optional initial data array, typically from a
 * server response, or constructed directly in Javascript. The array is used
 * in place and becomes part of the constructed object. It is not cloned.
 * If no data is provided, the constructed object will be empty, but still
 * valid.
 * @extends {jspb.Message}
 * @constructor
 */
proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity = function(opt_data) {
  jspb.Message.initialize(this, opt_data, 0, -1, null, null);
};
goog.inherits(proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity, jspb.Message);
if (goog.DEBUG && !COMPILED) {
  proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity.displayName = 'proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity';
}


if (jspb.Message.GENERATE_TO_OBJECT) {
/**
 * Creates an object representation of this proto suitable for use in Soy templates.
 * Field names that are reserved in JavaScript and will be renamed to pb_name.
 * To access a reserved field use, foo.pb_<name>, eg, foo.pb_default.
 * For the list of reserved names please see:
 *     com.google.apps.jspb.JsClassTemplate.JS_RESERVED_WORDS.
 * @param {boolean=} opt_includeInstance Whether to include the JSPB instance
 *     for transitional soy proto support: http://goto/soy-param-migration
 * @return {!Object}
 */
proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity.prototype.toObject = function(opt_includeInstance) {
  return proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity.toObject(opt_includeInstance, this);
};


/**
 * Static version of the {@see toObject} method.
 * @param {boolean|undefined} includeInstance Whether to include the JSPB
 *     instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @param {!proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity} msg The msg instance to transform.
 * @return {!Object}
 * @suppress {unusedLocalVariables} f is only used for nested messages
 */
proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity.toObject = function(includeInstance, msg) {
  var f, obj = {
    sessionid: jspb.Message.getFieldWithDefault(msg, 1, ""),
    publickey: msg.getPublickey_asB64()
  };

  if (includeInstance) {
    obj.$jspbMessageInstance = msg;
  }
  return obj;
};
}


/**
 * Deserializes binary data (in protobuf wire format).
 * @param {jspb.ByteSource} bytes The bytes to deserialize.
 * @return {!proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity}
 */
proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity.deserializeBinary = function(bytes) {
  var reader = new jspb.BinaryReader(bytes);
  var msg = new proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity;
  return proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity.deserializeBinaryFromReader(msg, reader);
};


/**
 * Deserializes binary data (in protobuf wire format) from the
 * given reader into the given message object.
 * @param {!proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity} msg The message object to deserialize into.
 * @param {!jspb.BinaryReader} reader The BinaryReader to use.
 * @return {!proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity}
 */
proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity.deserializeBinaryFromReader = function(msg, reader) {
  while (reader.nextField()) {
    if (reader.isEndGroup()) {
      break;
    }
    var field = reader.getFieldNumber();
    switch (field) {
    case 1:
      var value = /** @type {string} */ (reader.readString());
      msg.setSessionid(value);
      break;
    case 2:
      var value = /** @type {!Uint8Array} */ (reader.readBytes());
      msg.setPublickey(value);
      break;
    default:
      reader.skipField();
      break;
    }
  }
  return msg;
};


/**
 * Serializes the message to binary data (in protobuf wire format).
 * @return {!Uint8Array}
 */
proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity.prototype.serializeBinary = function() {
  var writer = new jspb.BinaryWriter();
  proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity.serializeBinaryToWriter(this, writer);
  return writer.getResultBuffer();
};


/**
 * Serializes the given message to binary data (in protobuf wire
 * format), writing to the given BinaryWriter.
 * @param {!proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity} message
 * @param {!jspb.BinaryWriter} writer
 * @suppress {unusedLocalVariables} f is only used for nested messages
 */
proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity.serializeBinaryToWriter = function(message, writer) {
  var f = undefined;
  f = message.getSessionid();
  if (f.length > 0) {
    writer.writeString(
      1,
      f
    );
  }
  f = message.getPublickey_asU8();
  if (f.length > 0) {
    writer.writeBytes(
      2,
      f
    );
  }
};


/**
 * optional string sessionId = 1;
 * @return {string}
 */
proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity.prototype.getSessionid = function() {
  return /** @type {string} */ (jspb.Message.getFieldWithDefault(this, 1, ""));
};


/** @param {string} value */
proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity.prototype.setSessionid = function(value) {
  jspb.Message.setProto3StringField(this, 1, value);
};


/**
 * optional bytes publicKey = 2;
 * @return {!(string|Uint8Array)}
 */
proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity.prototype.getPublickey = function() {
  return /** @type {!(string|Uint8Array)} */ (jspb.Message.getFieldWithDefault(this, 2, ""));
};


/**
 * optional bytes publicKey = 2;
 * This is a type-conversion wrapper around `getPublickey()`
 * @return {string}
 */
proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity.prototype.getPublickey_asB64 = function() {
  return /** @type {string} */ (jspb.Message.bytesAsB64(
      this.getPublickey()));
};


/**
 * optional bytes publicKey = 2;
 * Note that Uint8Array is not supported on all browsers.
 * @see http://caniuse.com/Uint8Array
 * This is a type-conversion wrapper around `getPublickey()`
 * @return {!Uint8Array}
 */
proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity.prototype.getPublickey_asU8 = function() {
  return /** @type {!Uint8Array} */ (jspb.Message.bytesAsU8(
      this.getPublickey()));
};


/** @param {!(string|Uint8Array)} value */
proto.pt.ulisboa.tecnico.cross.contract.CryptoIdentity.prototype.setPublickey = function(value) {
  jspb.Message.setProto3BytesField(this, 2, value);
};



/**
 * Generated by JsPbCodeGenerator.
 * @param {Array=} opt_data Optional initial data array, typically from a
 * server response, or constructed directly in Javascript. The array is used
 * in place and becomes part of the constructed object. It is not cloned.
 * If no data is provided, the constructed object will be empty, but still
 * valid.
 * @extends {jspb.Message}
 * @constructor
 */
proto.pt.ulisboa.tecnico.cross.contract.Authorization = function(opt_data) {
  jspb.Message.initialize(this, opt_data, 0, -1, null, null);
};
goog.inherits(proto.pt.ulisboa.tecnico.cross.contract.Authorization, jspb.Message);
if (goog.DEBUG && !COMPILED) {
  proto.pt.ulisboa.tecnico.cross.contract.Authorization.displayName = 'proto.pt.ulisboa.tecnico.cross.contract.Authorization';
}


if (jspb.Message.GENERATE_TO_OBJECT) {
/**
 * Creates an object representation of this proto suitable for use in Soy templates.
 * Field names that are reserved in JavaScript and will be renamed to pb_name.
 * To access a reserved field use, foo.pb_<name>, eg, foo.pb_default.
 * For the list of reserved names please see:
 *     com.google.apps.jspb.JsClassTemplate.JS_RESERVED_WORDS.
 * @param {boolean=} opt_includeInstance Whether to include the JSPB instance
 *     for transitional soy proto support: http://goto/soy-param-migration
 * @return {!Object}
 */
proto.pt.ulisboa.tecnico.cross.contract.Authorization.prototype.toObject = function(opt_includeInstance) {
  return proto.pt.ulisboa.tecnico.cross.contract.Authorization.toObject(opt_includeInstance, this);
};


/**
 * Static version of the {@see toObject} method.
 * @param {boolean|undefined} includeInstance Whether to include the JSPB
 *     instance for transitional soy proto support:
 *     http://goto/soy-param-migration
 * @param {!proto.pt.ulisboa.tecnico.cross.contract.Authorization} msg The msg instance to transform.
 * @return {!Object}
 * @suppress {unusedLocalVariables} f is only used for nested messages
 */
proto.pt.ulisboa.tecnico.cross.contract.Authorization.toObject = function(includeInstance, msg) {
  var f, obj = {
    jwt: jspb.Message.getFieldWithDefault(msg, 1, ""),
    servercertificate: msg.getServercertificate_asB64()
  };

  if (includeInstance) {
    obj.$jspbMessageInstance = msg;
  }
  return obj;
};
}


/**
 * Deserializes binary data (in protobuf wire format).
 * @param {jspb.ByteSource} bytes The bytes to deserialize.
 * @return {!proto.pt.ulisboa.tecnico.cross.contract.Authorization}
 */
proto.pt.ulisboa.tecnico.cross.contract.Authorization.deserializeBinary = function(bytes) {
  var reader = new jspb.BinaryReader(bytes);
  var msg = new proto.pt.ulisboa.tecnico.cross.contract.Authorization;
  return proto.pt.ulisboa.tecnico.cross.contract.Authorization.deserializeBinaryFromReader(msg, reader);
};


/**
 * Deserializes binary data (in protobuf wire format) from the
 * given reader into the given message object.
 * @param {!proto.pt.ulisboa.tecnico.cross.contract.Authorization} msg The message object to deserialize into.
 * @param {!jspb.BinaryReader} reader The BinaryReader to use.
 * @return {!proto.pt.ulisboa.tecnico.cross.contract.Authorization}
 */
proto.pt.ulisboa.tecnico.cross.contract.Authorization.deserializeBinaryFromReader = function(msg, reader) {
  while (reader.nextField()) {
    if (reader.isEndGroup()) {
      break;
    }
    var field = reader.getFieldNumber();
    switch (field) {
    case 1:
      var value = /** @type {string} */ (reader.readString());
      msg.setJwt(value);
      break;
    case 2:
      var value = /** @type {!Uint8Array} */ (reader.readBytes());
      msg.setServercertificate(value);
      break;
    default:
      reader.skipField();
      break;
    }
  }
  return msg;
};


/**
 * Serializes the message to binary data (in protobuf wire format).
 * @return {!Uint8Array}
 */
proto.pt.ulisboa.tecnico.cross.contract.Authorization.prototype.serializeBinary = function() {
  var writer = new jspb.BinaryWriter();
  proto.pt.ulisboa.tecnico.cross.contract.Authorization.serializeBinaryToWriter(this, writer);
  return writer.getResultBuffer();
};


/**
 * Serializes the given message to binary data (in protobuf wire
 * format), writing to the given BinaryWriter.
 * @param {!proto.pt.ulisboa.tecnico.cross.contract.Authorization} message
 * @param {!jspb.BinaryWriter} writer
 * @suppress {unusedLocalVariables} f is only used for nested messages
 */
proto.pt.ulisboa.tecnico.cross.contract.Authorization.serializeBinaryToWriter = function(message, writer) {
  var f = undefined;
  f = message.getJwt();
  if (f.length > 0) {
    writer.writeString(
      1,
      f
    );
  }
  f = message.getServercertificate_asU8();
  if (f.length > 0) {
    writer.writeBytes(
      2,
      f
    );
  }
};


/**
 * optional string jwt = 1;
 * @return {string}
 */
proto.pt.ulisboa.tecnico.cross.contract.Authorization.prototype.getJwt = function() {
  return /** @type {string} */ (jspb.Message.getFieldWithDefault(this, 1, ""));
};


/** @param {string} value */
proto.pt.ulisboa.tecnico.cross.contract.Authorization.prototype.setJwt = function(value) {
  jspb.Message.setProto3StringField(this, 1, value);
};


/**
 * optional bytes serverCertificate = 2;
 * @return {!(string|Uint8Array)}
 */
proto.pt.ulisboa.tecnico.cross.contract.Authorization.prototype.getServercertificate = function() {
  return /** @type {!(string|Uint8Array)} */ (jspb.Message.getFieldWithDefault(this, 2, ""));
};


/**
 * optional bytes serverCertificate = 2;
 * This is a type-conversion wrapper around `getServercertificate()`
 * @return {string}
 */
proto.pt.ulisboa.tecnico.cross.contract.Authorization.prototype.getServercertificate_asB64 = function() {
  return /** @type {string} */ (jspb.Message.bytesAsB64(
      this.getServercertificate()));
};


/**
 * optional bytes serverCertificate = 2;
 * Note that Uint8Array is not supported on all browsers.
 * @see http://caniuse.com/Uint8Array
 * This is a type-conversion wrapper around `getServercertificate()`
 * @return {!Uint8Array}
 */
proto.pt.ulisboa.tecnico.cross.contract.Authorization.prototype.getServercertificate_asU8 = function() {
  return /** @type {!Uint8Array} */ (jspb.Message.bytesAsU8(
      this.getServercertificate()));
};


/** @param {!(string|Uint8Array)} value */
proto.pt.ulisboa.tecnico.cross.contract.Authorization.prototype.setServercertificate = function(value) {
  jspb.Message.setProto3BytesField(this, 2, value);
};


goog.object.extend(exports, proto.pt.ulisboa.tecnico.cross.contract);