use messagepack_rs::extension::Extension;
use messagepack_rs::stream::serializer::Serializer;
use messagepack_rs::value::Value;
use messagepack_rs::binary::Binary;
use messagepack_rs_macros::MessagePackFrom;
use std::io::{self, Read, Write};
use std::collections::BTreeMap;
use chrono::prelude::*;
use std::thread::sleep;
use std::time;

#[derive(Clone, Debug, PartialEq, MessagePackFrom)]
enum MyValue {
    Nil,
    Bool(bool),
    Float32(f32),
    Float64(f64),
    UInt8(u8),
    UInt16(u16),
    UInt32(u32),
    UInt64(u64),
    Int8(i8),
    Int16(i16),
    Int32(i32),
    Int64(i64),
    Binary(Binary),
    String(String),
    Array(Vec<Self>),
    Map(BTreeMap<String, Self>),
    Extension(Extension),
    Timestamp(DateTime<Utc>),
}

fn main() {
    let mut buffer = String::new();
    let stdin = io::stdin();
    let stdout = io::stdout();
    let mut out = stdout.lock();

    {
        let input = stdin.lock();
        let x = input.bytes();

        let buf: Vec<u8> = Vec::new();
        let mut stream_serializer = Serializer::new(buf);
        stream_serializer.serialize(Value::from(true)).unwrap();
        stream_serializer.serialize(Value::from(false)).unwrap();
        stream_serializer.serialize(Value::Nil).unwrap();
        stream_serializer.flush().unwrap();
        out.write_all(stream_serializer.get_ref());
        out.flush();
    }
    loop {
        sleep(time::Duration::from_millis(100));
    }
}
