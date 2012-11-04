#include "wrangleplow.h"
#include <QApplication>

int main(int argc, char *argv[])
{
  QApplication app(argc, argv);

  WranglePlow::MainWindow wrangleplow;
  wrangleplow.show();

  return app.exec();
}