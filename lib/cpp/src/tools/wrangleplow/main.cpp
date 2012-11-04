#include "wrangleplow.h"
#include <QApplication>

int main(int argc, char *argv[])
{
  QApplication app(argc, argv);

  WranglePlow::MainWindow wrangleplow;
  wrangleplow.show();
  wrangleplow.resize(1024, 768);

  return app.exec();
}